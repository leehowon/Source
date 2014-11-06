package kcp.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class RemoteUtil
{
    public final Log logger = LogFactory.getLog( this.getClass() );
    
    private static final String SSH = "exec";
    private static final String SFTP = "sftp";
    
    private String host = null;
    private String userId = null;
    private String userPw = null;
    
    private int port = 0;
    
    private JSch jsch = new JSch();
    private Session session = null;
    
    private RemoteUtil(){}
    
    public static void main( String[] ar ) throws Exception
    {
        RemoteUtil remoteUtil = new RemoteUtil( "ip", "id", "pw" );
        String javaBinPath = "/usr/local/jdk/bin/"
                , processId = remoteUtil.execute( javaBinPath + "jps -v | awk '{print $1}'" );
        
        if( processId != null )
        {
            String[] processIds = processId.trim().split( "\r\n" );
            
            for( String id : processIds )
            {
                Thread.sleep( 100 );
                String command = String.format( javaBinPath + "jmap -heap %s", id );
                System.out.println( command );
                System.out.println( remoteUtil.execute(command) );
            }
        }
        
    }
    
    public RemoteUtil( String ip, String id, String pw ) throws Exception
    {
        this.host = ip;
        this.userId = id;
        this.userPw = pw;
        
        sessionConnect( host, port, userId, userPw );
    }
    
    public RemoteUtil( String ip, int port, String id, String pw ) throws Exception
    {
        this.host = ip;
        this.port = port;
        this.userId = id;
        this.userPw = pw;
        
        sessionConnect( host, port, userId, userPw );
    }

    public void sessionConnect() throws Exception
    {
        sessionConnect( null, 0, null, null );
    }
    
    public void sessionConnect( String ip, int port, String id, String pw ) throws Exception
    {
        //세션이 없거나 새로 접속 하려고 할 때
        if( session == null 
                || (ip != null && id != null && pw != null) )
        {
            sessionDisconnect();
            
            Properties properties = new Properties();
            
            session = jsch.getSession( id, ip );
            properties.put( "StrictHostKeyChecking", "no" );
            session.setPassword( pw );
            session.setConfig( properties );
        }

        if( !session.isConnected() )
            session.connect();
    }
    
    public void sessionDisconnect()
    {
        if( session != null && session.isConnected() )
            session.disconnect();
    }

    public String execute( String command ) throws Exception
    {
        return execute( new String[]{command} );
    }

    public String execute( String[] commands ) throws Exception
    {
        sessionConnect();
        
        String output = "";
        
        for( String command : commands )
        {
            ChannelExec exec = ( ChannelExec ) session.openChannel( SSH );
            InputStream input = exec.getInputStream()
                    , error = exec.getErrStream();
            
            exec.setPty( true );
            exec.setCommand( command );
            exec.connect( 30000 );
            
            output = IOUtils.toString( input );
            
            logger.debug( "Output : \n" + output );
            logger.debug( "Error : \n" + IOUtils.toString(error) );
            
            exec.disconnect();
        }
        
        return output;
        //sessionDisconnect();
    }
    
    public void get( String filePath, String downloadPath ) throws Exception
    {
        sessionConnect();
        
        ChannelSftp sftp = ( ChannelSftp ) session.openChannel( SFTP );
        sftp.connect( 30000 );
        
        InputStream is = null;
        FileOutputStream fos = null;
        
        try
        {
            is = sftp.get( filePath ); // path + filename
            fos = new FileOutputStream( new File(downloadPath) ); // path + filename
            int i = -1;
            sftp.pwd();
            while ( (i = is.read()) != -1 )
                fos.write( i );
        }
        catch( Exception e )
        {
            throw e;
        }
        finally
        {
            if( is != null ) is.close();
            if( fos != null ) fos.close();
        }
    }
    /**
    public void put( String uploadPath, File file ) throws Exception
    {
        sessionConnect();
        
        FileInputStream fi = new FileInputStream(file);
        ChannelSftp sftp = ( ChannelSftp ) session.openChannel( SFTP );
        sftp.connect( 30000 );
        
        sftp.cd( uploadPath );
        sftp.put( fi, file.getName() );
        fi.close();
    }
    /**/
}
