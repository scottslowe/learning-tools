# sc_ssl_support
# Script Tested using Ubuntu 16.04 LTS server.   

USE AT YOUR OWN RISK!   
This script is a WORK AROUND to fixing this issue by using NGINX as a reverse proxy rather then using SC's Mono.
THERE MIGHT BE BUGS IN THIS SCRIPT!
This is an attempt to try to make it easier for the people that have no idea what they are doing.
This assumes your using a debian/ubuntu based OS, and probably more ubuntu then debian.
This assumes your default install path is /opt/screenconnect/  
This assumes your going to use letsencrypt for a free ssl certificate
When the script asks you for hostname you should input the exact hostname your using for your screenconnect on prem 
i.e.      help.mydomain.com 

# make script executable 
chmod +x  sc_ssl_script_v2



# run the script
./sc_ssl_script_v2
