#!/usr/bin/python

import sys;
import re;

def main(argv):

	regex = re.compile('(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+)');
	line =  sys.stdin.readline();
	try:
		while line:
			fields = regex.match(line);
			if (fields!=None):
				print "LongValueSum:"+fields.group(1) + "\t" + fields.group(7); 
			line = sys.stdin.readline();
	except "end of file":
		return None

if  __name__ == "__main__":
	main(sys.argv)
