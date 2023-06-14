#======================== Get RabbitMQ: ========================
#https://www.rabbitmq.com/getstarted.html


#======================== Install RabbitMQ: ========================
# Mac OS
$ brew update
$ brew install rabbitmq

#The RabbitMQ server scripts are installed into /usr/local/sbin;
#This is NOT automatically added to path (so add it through .bash_profile or .profile):
export PATH=${PATH}:/usr/local/sbin

==> Summary
 /usr/local/Cellar/rabbitmq/3.7.4

#======================== Config File: ========================
#Can change credentials, etc.
$ more /usr/local/etc/rabbitmq/rabbitmq-env.conf

#======================== Runtime monitoring and config: ========================
# Management UI (username/passwd = guest/guest -> guest/guest4rabbitmq)
# Management Plugin enabled by default at:
http://localhost:15672/

#Bash completion has been installed to:
  /usr/local/etc/bash_completion.d

#======================== Launch start rabbitmq: ========================
#1. As service (and restart at login):
$ brew services start rabbitmq

#2. As a shell process:
$ rabbitmq-server


#On UNIX systems, the cookie will be typically located in:
# /var/lib/rabbitmq/.erlang.cookie (used by the server)
# ${HOME}/.erlang.cookie (used by CLI tools)
//NB: necessary to place a copy of the cookie file for each user

#======================== CLI management: ========================
#1. rabbitmqctl
$ rabbitmqctl list_users
$ rabbitmqctl change_password guest guest4rabbitmq

#2. rabbitmq-diagnostics
$ rabbitmq-diagnostics list_queues

#3. rabbitmq-plugins...
#Activate management plugin
$ rabbitmq-plugins enable rabbitmq_management
#List installed plugins
$ rabbitmq-plugins list
