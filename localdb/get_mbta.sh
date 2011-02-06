#!/bin/sh

get_route_list()
{
    echo -n "Getting route list... "
    wget -q -O data/routelist.xml 'http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=mbta'
    echo "done."
}

get_all_route_nums() 
{
    cat data/routelist.xml | grep '<route' | awk '{print $2}' | awk -F= '{print $2}' | sed 's/"//g' 
}

get_data_for_all_routes()
{
    get_all_route_nums | while read routenum
    do
	echo -n "Getting route $routenum... "
	wget -q -O "data/route${routenum}.xml" 'http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r='"$routenum"
	echo "done."
    done
}

build_overview() 
{
    echo -n "Buiding overview file... "
    echo '<?xml version="1.0" encoding="utf-8" ?>' > data/overview.xml
    echo '<body copyright="All data copyright MBTA">' >> data/overview.xml
    get_all_route_nums | while read routenum
    do
	cat "data/route${routenum}.xml" | grep '<route' >> data/overview.xml
	echo "</route>" >> data/overview.xml
    done
    echo '</body>' >> data/overview.xml
    echo "done."
}

get_route_list
get_data_for_all_routes
build_overview

