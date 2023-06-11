#!/usr/bin/env bash
trap cleanup 1 2 3 6

cleanup (){
  pkill -f 'python'
  pkill -f 'rmiregistry'
  pkill -f 'java'
}

#compile
compile(){
  javac -d . ../server/*.java  ../client/*.java ../../util/rmisetup/*.java
}

#runpython
run_python(){
    sh _1_runpython.sh &
}

#rmiregisty
start_registry(){
  pkill -f 'rmiregistry'
  sh _2_runregistry.sh &
}

#server
start_server(){
  sh _3_runserver.sh &
}

start_client(){
  sh _4_runclient.sh
}


cleanup
compile
#sleep 1
#run_python
#sleep 2
start_registry
sleep 2
start_server

sleep 2




while true; do
    options=("Run Client" "Quit")

    echo "Choose an option:"
    select opt in "${options[@]}"; do
        case $REPLY in
            1) echo "Run Client"; start_client; break ;;
            2) exit 1; break ;;
            *) echo "invalid option $REPLY";;
        esac
    done
done