package kcp.common.utils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kcp.common.CommonConst;
import kcp.system.DefaultMap;
import webwork.action.ServletActionContext;

public class MenuPowerUtil
{
    private static final boolean IS_STATIC_PWR_MAP = true; //전역 권한 맵 사용 여부( false일 경우 사용자 별 SessionMap에 권한이 담긴다. )
    
    private static final long DEFAULT_POWER_VALUE = 0; //권한 없는 값
    
    private static final String URL_KEY = "mnu_url"; //Login.getMenuPowerListByGroupNo 쿼리에 있는 column 명
    private static final String POWER_KEY = "mnu_pwr";
    private static final String MENU_KEY = "mnu_cd";
    
    private static DefaultMap GROUP_POWER_MAP = IS_STATIC_PWR_MAP ? new DefaultMap() : null; //권한 맵
    
    private MenuPowerUtil(){}
    
    private static HttpSession getSession()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        
        return request == null ? null : request.getSession( true );
    }
    
    private static DefaultMap getUrlMap()
    {
        HttpSession session = getSession();
        DefaultMap urlMap = null;
        
        if( session == null ) return new DefaultMap();
        
        if( IS_STATIC_PWR_MAP )
            urlMap = ( DefaultMap ) GROUP_POWER_MAP.get( session.getAttribute(CommonConst.SESSION_ADMIN_GP_NO) );
        else
            urlMap = ( DefaultMap ) session.getAttribute( CommonConst.SESSION_MENU_URL_POWER );
        
        return urlMap == null ? new DefaultMap() : urlMap;
    }
    
    private static DefaultMap getPowerMap( String url )
    {
        DefaultMap urlMap = getUrlMap()
                , powerMap = ( DefaultMap ) urlMap.get( url.replace("/", "") );

        return powerMap == null ? new DefaultMap() : powerMap;
    }
    
    private static boolean comparePower( Long userPower, long typePower )
    {
        return (userPower & typePower) == typePower;
    }
    
    private static String urlJoin( String requestURI, String[] params )
    {
        StringBuffer sb = new StringBuffer( requestURI ).append( "?" );
        
        for( int li = 0, limit = params.length; li < limit; ++li )
            sb.append( params[li] ).append( li % 2 == 0 ? '=' : '&' );
        
        int length = sb.length();
        
        return sb.delete( length - 1, length ).toString();
    }

    /**
     * 권한 맵 변경
     * 
     * @param list
     * @param isGroupCheck
     */
    public static void putPowerMap( List<DefaultMap> list, boolean isGroupCheck )
    {
        HttpSession session = getSession();
        
        if( session == null ) return;
        
        String adminGroupNo = session.getAttribute( CommonConst.SESSION_ADMIN_GP_NO ).toString();
        //전역 권한 맵을 사용하고 권한을 추가하기 전에 체크 할 것인지 여부가 true이고 그룹의 권한이 이미 있다면 맵에 담지 않는다.
        if( IS_STATIC_PWR_MAP && isGroupCheck && GROUP_POWER_MAP.containsKey(adminGroupNo) )
                return;

        DefaultMap urlMap = new DefaultMap();
        
        for( DefaultMap menuPower : list )
            urlMap.put( menuPower.getStr(URL_KEY), menuPower );
        
        if( IS_STATIC_PWR_MAP )
        {
            if( !isGroupCheck ) //static은 clear() 메서드로만 삭제 가능
            {
                DefaultMap tempMap = ( DefaultMap ) GROUP_POWER_MAP.clone();
                tempMap.put( adminGroupNo, urlMap );
                GROUP_POWER_MAP.clear();
                GROUP_POWER_MAP = tempMap;
            }
            else
                GROUP_POWER_MAP.put( adminGroupNo, urlMap );
        }
        else
            session.setAttribute( CommonConst.SESSION_MENU_URL_POWER, urlMap );

    }
    
    public static boolean isAllowPower( long power, String requestURI, String[] params )
    {
        return isAllowPower( power, urlJoin(requestURI, params) );
    }
    
    /**
     * 권한 확인하기
     * 
     * @param power
     * @param url
     * @return
     */
    public static boolean isAllowPower( long power, String url )
    {
        return comparePower( getPowerMap(url).getLong(POWER_KEY, DEFAULT_POWER_VALUE), power );
    }
    
    public static DefaultMap getPower( String requestURI, String[] params )
    {
        return getPower( urlJoin(requestURI, params) );
    }
    
    /**
     * 권한 맵 가져오기
     * 
     * @param url
     * @return
     */
    public static DefaultMap getPower( String url )
    {
        DefaultMap menuPowerMap = getPowerMap( url )
                   , dMap = new DefaultMap();
        Long userPower = menuPowerMap.getLong( POWER_KEY, DEFAULT_POWER_VALUE );

        dMap.put( CommonConst.MENU_CD_KEY, menuPowerMap.getStr(MENU_KEY) );
        dMap.put( CommonConst.DIS_KEY, comparePower(userPower, CommonConst.DIS_PWR) );
        dMap.put( CommonConst.REG_KEY, comparePower(userPower, CommonConst.REG_PWR) );
        dMap.put( CommonConst.MOD_KEY, comparePower(userPower, CommonConst.MOD_PWR) );
        dMap.put( CommonConst.DEL_KEY, comparePower(userPower, CommonConst.DEL_PWR) );
        dMap.put( CommonConst.XLS_KEY, comparePower(userPower, CommonConst.XLS_PWR) );
        dMap.put( CommonConst.PRIVATE_DIS_KEY, comparePower(userPower, CommonConst.PRIVATE_DIS_PWR) );
        dMap.put( CommonConst.PRIVATE_XLS_KEY, comparePower(userPower, CommonConst.PRIVATE_XLS_PWR) );
        
        return dMap;
    }
}
