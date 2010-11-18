
ZOO_HOST="$1"
if [ -z "$ZOO_HOST%" ]; then
	ZOO_HOST="127.0.0.1:2181"
fi	

java -cp "../lib/*" $JVMFLAGS net.isammoc.zooviewer.App "$ZOO_HOST"


