# private ip configuration
IPADDR=$(ip -o route get to 8.8.8.8 | sed -n 's/.*src \([0-9.]\+\).*/\1/p')
# public ip configuration
#IPADDR=$(curl ifconfig.me && echo "")
NODENAME=$(hostname -s)
POD_CIDR="192.168.0.0/24"

#private ip address-based setup
## with containerd
#sudo kubeadm init --cri-socket unix:///var/run/containerd/containerd.sock --apiserver-advertise-address=$IPADDR  --apiserver-cert-extra-sans=$IPADDR  --pod-network-cidr=$POD_CIDR --node-name $NODENAME --ignore-preflight-errors Swap
## with crio 
sudo kubeadm init --cri-socket unix:///var/run/crio/crio.sock --apiserver-advertise-address=$IPADDR  --apiserver-cert-extra-sans=$IPADDR  --pod-network-cidr=$POD_CIDR --node-name $NODENAME --ignore-preflight-errors Swap
#public ip address-based setup
## with containerd
#sudo kubeadm init --cri-socket unix:///var/run/containerd/containerd.sock --control-plane-endpoint=$IPADDR  --apiserver-cert-extra-sans=$IPADDR  --pod-network-cidr=$POD_CIDR --node-name $NODENAME --ignore-preflight-errors Swap
## with crio
#sudo kubeadm init --cri-socket unix:///var/run/crio/crio.sock --control-plane-endpoint=$IPADDR  --apiserver-cert-extra-sans=$IPADDR  --pod-network-cidr=$POD_CIDR --node-name $NODENAME --ignore-preflight-errors Swap
