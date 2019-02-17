set -ux

echo "Environment is:-"
env

source /etc/os-release

cat << EOT > /etc/motd
        _       _                  _    ___      
   __ _| |_ __ (_)_ __   ___      | | _( _ ) ___ 
  / _\` | | '_ \\| | '_ \\ / _ \\_____| |/ / _ \\/ __|
 | (_| | | |_) | | | | |  __/_____|   < (_) \\__ \\
  \\__,_|_| .__/|_|_| |_|\\___|     |_|\\_\\___/|___/
         |_|                     KUBERNETES SERVER

Alpine: ${VERSION_ID}
Docker: ${DOCKER_VERSION}
Kubernetes: ${KUBERNETES_VERSION}

See build and usage instructions at:
  <https://github.com/davidmccormick/alpine-k8s>

EOT
