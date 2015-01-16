function param2json( strParam ){
    if( typeof strParam !== "string" ) return strParam;

    var params = strParam.split( "&" )
        , obj = {};

    for( var li in params ){
        var variable = params[ li ].split( "=" )
            , varName = variable[ 0 ]
            , val = variable[ 1 ];

        if( !varName ) continue;

        if( !obj.hasOwnProperty(varName) ){
            obj[ varName ] = val;
            continue;
        }

        var arr = obj[ varName ];

        if( arr instanceof Array )
            arr[ arr.length ] = val
        else
            obj[ varName ] = [ arr, val ]; //중복되는 값이 생겼을 때 배열로 생성하면서 기존 값, 새로 넣을 값을 추가
    }

    return obj;
}

function sendData( $, url, param, options ){
    if( !url || !$ instanceof Object || !$.prototype.jquery ) return; //TODO. jQuery 없이도 동작할 수 있도록 변경 해야 함

    param = param2json( param );
    options = param2json( options );

    if( typeof param !== "object" ) return;

    var formId = "testForm" + new Date().getTime()
        , formSelector = "#" + formId
        , method = options.method || "post"
        , target = options.target || ""
        , textHTML = [ "<form id=\"", formId, "\" method=\"" + method + "\" target=\"" + target + "\" action=\"", url, "\">" ];

    if( options.type === "popup" ){
        var width = options.width || 600
            , height = options.height || 500
            , top = options.top || screen.availWidth / 2 - ( width / 2 )
            , left = options.left || screen.availHeight / 2 - ( height / 2 )
            , popupOptions = "width=" + width + ",height=" + height + ",top=" + top + ",left=" + left + ",scrollbars=1,resizable=1,status=1,toolbar=0,menubar=0";

        window.open( "", target, popupOptions );
    }

    for( var li in param )
        textHTML[ textHTML.length ] = "<input type=\"hidden\" name=\"" + li + "\" value=\"" + param[ li ] + "\" />";

    textHTML[ textHTML.length ] = "</form>";
    $( "body" ).append( textHTML.join("") ); //TODO. jQuery 없을 때 native code 로 처리 하는 로직 추가 해야함
    $( formSelector )
                    .submit()
                    .remove();
}
