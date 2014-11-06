package kcp.common.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kcp.common.CommonConst;
import kcp.system.DefaultMap;
import webwork.action.ServletActionContext;

public class MenuPowerUtil
{
    private static final long DEFAULT_POWER_VALUE = 0; //권한 없는 값
    
    private static final String URL_KEY = "mnu_url"; //Login.getMenuPowerListByGroupNo 쿼리에 있는 column 명
    private static final String POWER_KEY = "mnu_pwr";
    private static final String MENU_KEY = "mnu_cd";
    
    private static final Map< String, Object > GROUP_POWER_MAP = new ConcurrentHashMap< String, Object >(); //권한 맵
    private MenuPowerUtil(){}

    /**
     * 새로운 세션 가져오기
     * 
     * @return HttpSession
     */
    private static HttpSession getNewSession()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        
        return request == null ? null : request.getSession( true );
    }
    
    private static Map< String, Object > getNewMap()
    {
        return new DefaultMap();
    }
    
    /**
     * 내가 소속한 그룹의 권한맵 가져오기
     * 
     * @return Map< String, Object >
     */
    private static Map< String, Object > getMyGroupPowerMap()
    {
        HttpSession session = getNewSession();

        if( session == null ) return getNewMap();
        
        Object power = GROUP_POWER_MAP.get( session.getAttribute(CommonConst.SESSION_ADMIN_GP_NO) );

        return power == null ? getNewMap() : ( Map<String, Object> ) power;
    }
    
    /**
     * 권한 맵 가져오기
     * 
     * @param url
     * @return Map< String, Object >
     */
    private static Map< String, Object > getPowerMap( String url )
    {
        Object powerMap = getMyGroupPowerMap().get( url.replace("/", "") );

        return powerMap == null ? getNewMap() : ( Map<String, Object> ) powerMap;
    }
    
    /**
     * 권한 비교
     * 
     * @param userPower
     * @param typePower
     * @return boolean
     */
    private static boolean comparePower( Long userPower, long typePower )
    {
        // 신규메뉴 사용시
        return (userPower & typePower) == typePower;
        
        // 신규메뉴 문제 발생시 기존으로 돌릴때.
        //return true;
    }
    
    /**
     * URL 조합
     * 
     * @param requestURI
     * @param params
     * @return String
     */
    private static String urlJoin( String requestURI, String[] params )
    {
        if( params == null )
            return requestURI;
        
        StringBuffer sb = new StringBuffer( requestURI ).append( '?' );
        
        for( int li = 0, limit = params.length; li < limit; ++li )
            sb.append( params[li] ).append( li % 2 == 0 ? '=' : '&' );
        
        int length = sb.length();
        
        return sb.delete( length - 1, length ).toString();
    }

    /**
     * 권한 맵 변경
     * 
     * @param list
     * @param adminGroupNo
     * @param isGroupCheck
     */
    public static void putPowerMap( List<DefaultMap> list, String adminGroupNo, boolean isGroupCheck )
    {
        if( adminGroupNo == null ) return;
        //권한맵 수정하기전 키를 체크 할 것인지 여부가 참이고 그룹의 권한이 이미 있다면 맵에 담지 않는다.
        if( isGroupCheck && GROUP_POWER_MAP.containsKey(adminGroupNo) )
            return;

        Map< String, Object > urlMap = getNewMap()
                , tempMap = ( Map<String, Object> ) GROUP_POWER_MAP.get( adminGroupNo );
        
        for( DefaultMap menuPower : list )
            urlMap.put( menuPower.getStr(URL_KEY), menuPower );
        
        GROUP_POWER_MAP.put( adminGroupNo, urlMap );
        
        if( tempMap != null )
        {
            tempMap.clear();
            tempMap = null;
        }
    }
    
    /**
     * 권한 확인하기
     * 
     * @param power
     * @param url
     * @return boolean
     */
    public static boolean isAllowPower( long power, String url )
    {
        DefaultMap powerMap = ( DefaultMap ) getPowerMap( url );
        return comparePower( powerMap.getLong(POWER_KEY, DEFAULT_POWER_VALUE), power );
    }
    
    /**
     * 권한 확인하기
     * 
     * @param power
     * @param requestURI
     * @param params
     * @return boolean
     */
    public static boolean isAllowPower( long power, String requestURI, String[] params )
    {
        return isAllowPower( power, urlJoin(requestURI, params) );
    }

    /**
     * 권한 맵 가져오기
     * 
     * @param url
     * @return DefaultMap
     */
    public static DefaultMap getPower( String url )
    {
        DefaultMap powerMap = ( DefaultMap ) getPowerMap( url )
                , dMap = new DefaultMap();
        long userPower = powerMap.getLong( POWER_KEY, DEFAULT_POWER_VALUE );

        dMap.put( CommonConst.MENU_CD_KEY, powerMap.getStr(MENU_KEY) );
        dMap.put( CommonConst.DIS_KEY, comparePower(userPower, CommonConst.DIS_PWR) );
        dMap.put( CommonConst.REG_KEY, comparePower(userPower, CommonConst.REG_PWR) );
        dMap.put( CommonConst.MOD_KEY, comparePower(userPower, CommonConst.MOD_PWR) );
        dMap.put( CommonConst.DEL_KEY, comparePower(userPower, CommonConst.DEL_PWR) );
        dMap.put( CommonConst.XLS_KEY, comparePower(userPower, CommonConst.XLS_PWR) );
        dMap.put( CommonConst.PRIVATE_DIS_KEY, comparePower(userPower, CommonConst.PRIVATE_DIS_PWR) );
        dMap.put( CommonConst.PRIVATE_XLS_KEY, comparePower(userPower, CommonConst.PRIVATE_XLS_PWR) );
        
        return dMap;
    }
    
    /**
     * 권한 맵 가져오기
     * 
     * @param requestURI
     * @param params
     * @return DefaultMap
     */
    public static DefaultMap getPower( String requestURI, String[] params )
    {
        return getPower( urlJoin(requestURI, params) );
    }
}
