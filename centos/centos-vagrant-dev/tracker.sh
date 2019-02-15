#!/bin/bash
if [ ! -f ~/.config/autostart/trackerd.desktop ]
then
    echo Disabling Tracker
    echo https://askubuntu.com/questions/346211/tracker-store-and-tracker-miner-fs-eating-up-my-cpu-on-every-startup

    mkdir ~/.config/autostart
    # cd ~/.config/autostart
    tee -a ~/.config/autostart/trackerd.desktop <<EOF
[Desktop Entry]
Encoding=UTF-8
Name=Tracker
Hidden=true
EOF

    cd ~
fi
