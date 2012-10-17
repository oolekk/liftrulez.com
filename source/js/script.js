/*
 *= require libs/ba-debug.min.js
 *= require libs/prelude-browser-min.js
 */
(function(){
  var strip;
  import$(this, prelude);
  strip = curry$(function(input){
    return (input + "").replace(/^(\s)*/, '').replace(/(\s)*$/, '');
  });
  debug.info('loaded script.js');
  function import$(obj, src){
    var own = {}.hasOwnProperty;
    for (var key in src) if (own.call(src, key)) obj[key] = src[key];
    return obj;
  }
  function curry$(f, args){
    return f.length > 1 ? function(){
      var params = args ? args.concat() : [];
      return params.push.apply(params, arguments) < f.length && arguments.length ?
        curry$.call(this, f, params) : f.apply(this, params);
    } : f;
  }
}).call(this);
