# If you change, Keep the structure with the dot. [0-9 a-f]
$token = "abcdef.0123456789abcdef"

# Total memory of master
$master_memory = 6000

# Increment to have more nodes
$worker_count = 0

# Total memory of nodes
$worker_memory = 1536

# Add Grafana with InfluxDB (work with heapster)
$grafana = false

# Cluster IP Addresses
$cluster_ips = "192.168.56.224/28"
$metallb_ips = "192.168.56.240/28"

# Deploy Metal LB
$metallb = true

# Deploy Ingress Controller
$ingress_controller = "istio"

# Deploy Prometheus
$prometheus = false
