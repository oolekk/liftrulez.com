/*
 *= require libs/ba-debug.min.js
 *= require libs/prelude-browser-min.js
 */
import prelude
# make fixed elements scroll horizontally 

#this function will remove whitespaces at the begining and end of string
strip(input) = (input+"").replace(/^(\s)*/,'').replace(/(\s)*$/,'')

debug.info('loaded script.js')
