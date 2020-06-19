#sudo iptables -t nat -A POSTROUTING --destination 192.168.0.3/24 -j SNAT --to-source 192.168.0.4
#sudo iptables -t nat -A OUTPUT -d  192.168.0.4/24 -j DNAT --to-destination 127.0.0.1 
#sudo iptables -A FORWARD -i eth1 --source 123.123.1.4/24 -o 192.168.0.4 -m state --state RELATED,ESTABLISHED -j ACCEPT
#sudo iptables -A FORWARD -i eth1 --source 123.123.1.4/24 -o 192.168.0.4 -j ACCEPT

#sudo iptables -t nat -A PREROUTING -i eth1 -d --destination 192.168.0.4/24 -j SNAT
sudo iptables -t nat -A POSTROUTING --destination 192.168.0.0/24 -o eth1 -j SNAT --to 192.168.0.4
sudo iptables -t nat -A PREROUTING --source 192.168.0.0/24 -j DNAT --to 123.123.1.4
sudo iptables -t nat -A PREROUTING --source 123.123.1.1 -j DNAT --to 123.123.1.4
