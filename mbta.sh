#!/bin/bash


process()
{
    while read line
    do
	case "$line" in
	    \<predictions*) 
		route=$(echo "$line" | sed 's|^.*routeTitle="||' | awk -F\" '{print $1}')
		stop=$(echo "$line" | sed 's|^.*stopTitle="||' | awk -F\" '{print $1}')
		;;
	    \<direction*)
		direction=$(echo "$line" | sed 's|^.*title="||' | awk -F\" '{print $1}')
		;;
	    *predictions*)
		;;
	    *prediction*) 
		minutes=$(echo "$line" | sed 's|^.*minutes="||' | awk -F\" '{print $1}')
		echo "$minutes minutes: $route $direction: $stop"
		;;
	    *) 
		;;
	    
	esac
    done
}

get_data()
{
#    cat pred;
    wget http://webservices.nextbus.com/service/publicXMLFeed?command=predictionsForMultiStops\&a=mbta\&stops=748\|null\|2531\&stops=748\|null\|2612\&stops=91\|null\|2531\&stops=91\|null\|2612\&stops=86\|null\|25712\&stops=86\|null\|2615\&stops=85\|null\|2612\&stops=87\|null\|2510 -qO-
}

get_data  | process | sort -n
