Template = function( template ){ // String or jQuery HTML only
    function match( str ){
            var obj = {}
                , matches = undefined;

        while( (matches = _pattern.exec(str)) )
            obj[ matches[2] ] = matches[ 1 ];
            
        return obj;
    }
    
    var _template = (function( template ){
            var $ = window.jQuery || (function(){});
            
            if( template instanceof $ && $.prototype.jquery )
                return $( "<div/>" ).append( template ).html(); // template 사라짐
            else if( typeof template === "string" )
                return template;
            else
                return "";
        })( template )
        , _pattern = /(!\{(.*?)\})/g //!{PATTERN}
        , _patternObject = match( _template );
        
    return {
        getPattern: function(){
            return _pattern;
        }
        , match: function( str ){
            return match( str );
        }
        , parse: function( o ){
            var temp = _template;
            
            //"My name is !{name}. Your Name is !{name}".split( "!{name}" ).join( "text" )
            //output : "My name is text. Your Name is text"
            for( var val in o )
                temp = temp.split( _patternObject[val] )
                           .join( o[val] || (o[val] === 0 ? 0 : "") ); //new 생성시 정의했던 패턴 변수들만 적용된다.
                
            return temp.replace( _pattern, "" );
        }
    };
};
