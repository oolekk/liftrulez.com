liftrulez.com running on squeryl-record + embedded + jetty
==========================================================

There is embedded jetty and assembly sbt plugin for automated creation
of self-serving jar package.

This app is in a very early stage of development. The aim is to provide
Norton-Commander like experience, so that there are two panes for 
browsing the server directories. Functionality will be provided
to do copy/delete/rename/upload and do batch processing of whole trees
of images possibly storing useful data about images in custom file
attributes, so as a meta-data of the file itself without the need to
keep every detail in the database (for example image size in px or
hash sum).

This project front-end will be developed using haml and livescript
instead of html and javascript. LiveScript provides Haskel inpired
alternative to JavaScript and compiles down to regular JavaScript.
Additionaly prelude library is used to provide usual set of functional
style map/list operations.

To streamline CSS generation and CSS or JavaScript includes as well
as sprite image generation we use Middleman with Compass. Not much is
done so far about the frontend, but all the general setup for 
middleman is in place and already used.

Apart from showing sample setup of squeryl-record with different database
engines, this app includes a working configuration for building
self-serving jar with embedded jetty. It is a very lightweight and flexible
way to easily do deployment on any computer that has java. To create the
jar, in sbt run:

    assembly
    
Then cd to target dir and run:

    java -jar liftrulez.com-lift.jar
    
You can make it run in production mode like so:
    
    java -Drun.mode=production -jar liftrulez.com-lift.jar
    
You can give custom port number to be used by jetty:
    
    java -jar liftrulez.com-lift.jar 8090
    
I also made a script to make this app run as a normal linux service
installed under /etc/init.d , soon more about this. I was messing around
with the passwords a bit, so in case something is wrong, please check
properties files.
     
In development mode, h2 console will be browser accessible at:   
[http://localhost:9090/console](http://localhost:9090/console)  
You may need to change port number to port given on the command line when starting
executable jar or to default 8080 used by sbt if run from sbt. At the h2 console
login screen, JDBC URL must be adjusted to something like this:

    jdbc:h2:mem:liftrulez_com
    
Use login name 'test' and a blank password. This can be changed by
editing props files. 

### Credits

Embedded jetty configuration assembled by me with big help from Diego Medina
and Lift mailing list participants.
