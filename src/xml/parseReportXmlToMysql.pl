#!/usr/bin/perl
#
# Created by Adrien de Beaupre, adriendb (at) gmail (dot) com
# or adriendb (at) whitehats (dot) ca
#
# Version 0.01
# 15 December 2010
#
# Current version of this script is at http://handlers.dshield.org/adebeaupre/parsezaproxyxml2mysql.pl
#
# This script parses OWASP Zed Attack Proxy (zaproxy) XML reports and imports the results into a MySQL database. 
#

# Perl pragma/modules to use
use strict;								# Pragma to not do 'bad things' in perl.
use warnings;
use XML::DOM;							# Perl XML::DOM module Document Object Model to parse XML.
use DBI;								# Perl DBI module for database connectivity.
use Getopt::Long qw(:config ignore_case bundling);	# Perl Getopt module for CLI argument (configuration: case sensitive, and bundle options).
use Pod::Usage;							# Perl Pod::Usage module for printing out usage and documentation.

# Setup main variables for script arguments
my $dbname = 'zaproxydb';					# Declare the database name variable, default to 'zaproxydb' 
my $dbuser = 'zaproxyuser';					# Declare the database username variable, change the default or input at CLI!
my $dbpass = 's3kretp@ssw0rd';				# Declare the database password variable, change the default or input at CLI!
my $dbhost = 'localhost';					# Declare the database host variable, default to localhost.
my $dbport = "3306";						# Declare the variable for MySQL database listener port, default to 3306.
my $xmlfile;                         			# Declare the variable for the name of the zaproxy XML file to parse.
my $filelist;                         			# Declare the variable for a list of zaproxy XML files to parse.
my $filedir;                         			# Declare the variable for a directory of the zaproxy XML files to parse.
my $quiet;								# Declare the variable for setting the 'quiet' flag.
my $DEBUG;								# Declare the variable for setting the 'verbose' flag.

# Grab the options passed on the command line. 
GetOptions (
	"port|P=i" => \$dbport,					# numeric, database tcp port (3306).
	"database|d=s" => \$dbname, 				# string, database name (zaproxydb).
	"dir|D=s" => \$filedir,					# string, directory of reports to process.
	"user|u=s" => \$dbuser, 				# string, database username.
	"password|p=s" => \$dbpass,  				# string, database password.
	"host|H=s" => \$dbhost,					# string, database host IP or name (localhost).
	"quiet|q" => \$quiet,					# flag, quiet (only errors). 
	"file|f=s" => \$xmlfile,				# string, single report to process.
	"list|l=s" => \$filelist,				# string, list of reports to process.
	"initialize|i" => sub { &init_db; },		# subroutine, initialize (create) the database.
	"check|c" => sub { &check_db; },			# subroutine, check the database table and column names.
	"verbose|v" => \$DEBUG,					# flag, verbose (more output).
	"help|?|h" => sub { pod2usage(1); },		# subroutine, print out the usage documentation.
) or pod2usage("$0: Unrecognized program argument.");	# At least one CLI argument is invalid.

if( !( defined($xmlfile) or defined($filelist) or defined($filedir) ))	
									# One of -f, -l, or -D have to be specified to process files. 
{
	pod2usage("$0:  Required argument missing.");	# Unless -i or -c have been specified we need at least a zaproxy XML file to parse.
};									# dbuser, dbpass, dbhost, and dbport all have default values. 

my $dsn = "DBI:mysql:database=$dbname:host=$dbhost;port=$dbport";    					# Database Source Name.
my $dbh = DBI->connect($dsn, $dbuser, $dbpass, {'RaiseError' => 1, 'AutoCommit' => 0}) or 	# DataBase Handle
	die("\nERR:  Could not connect: $DBI::err: $DBI::errstr\n" ); 					# Error message of can't connect to database, exit. 

if (defined($filelist)) { &listfiles($filelist);}	# If -l was specified process the text file list, listfiles sub-routine, then return to exit. 
if (defined($xmlfile)) { &processfile($xmlfile);}	# If -f was specified process the one file, then return to exit. 
if (defined($filedir)) { &listdir($filedir);}		# If -D was specified list the directory, listdir sub-routine, then return to exit. 

$dbh->disconnect;							# Disconnect from the database.
exit (0);								# Exit. End of the main routine. 

sub listfiles 							# listfiles sub-routine. Parses a text file line by line, one report name on each.
{							
	my $list = shift;						# Name of list of zaproxy XML text file passed as an argument. 
	if (-e $list)						# Check to see if the file exists, if it doesn't then return.
	{
		open (FILE, $list);				# Open the file handle
 		while (<FILE>) 					# Loop through each line in the file
		{						
 			chomp;	
			my $line = $_;				# Grab the contents of the line. 			
			processfile($line);			# Process each file, calling the processfile sub-routine, then return for next.			
		};
	}
	else								# Error message, file does not exist. 
	{
		print "\nFile $list does not exist, cannot process it as a list of zaproxy XML reports. \n\n";	
	};
	close(FILE); 						# Close the file handle. 
	return;							# Return to main. 
};

sub listdir 							# listdir sub-routine. Lists the zaproxy XML files in a directory, processes them.
{
	my $dir = shift;						# Name of directory for zaproxy XML files to process
	my $file;							# Variable for each file in the directory
	if (-d $dir)						# Check to see if the directory exists, if it doesn't then return. 
	{
		opendir ( DIR, $dir );				# Open up the directory handle
		while( ($file = readdir(DIR))){		# Loop through each file
			next unless ($file =~ m/\.xml$/);	# Only process if the extension is .xml
			next if ($file =~ m/^\./);		# Don't process files that start with .
			my $wholefile = $dir."/".$file;	# Add the directory name for the path of the file to process
     			processfile($wholefile);		# Process each file, calling the processfile sub-routine, then return for next.
		};
	closedir(DIR);						# Close the directory handle.
	}
	else								# Error message, dir does not exist.
	{
		print "\nDirectory $dir does not exist, cannot process the contents as zaproxy XML reports. \n\n"; # Error message, dir does not exist.
	};
 	return;							# Return to main. 
}; 

sub processfile
{
	my $infile = shift;					# Name of the zaproxy XML file to process, passed as an argument. 
	unless (-e $infile) 					# Check to see if the file name exists.
	{
		print "\nFile $infile does not exist, cannot process the zaproxy XML report.\n\n" unless $quiet;
		return;						# Print a message and return if it doesn't. 
 	}; 
	# Start parsing XML
	my $parser = XML::DOM::Parser->new;  		# New XML::DOM parser object.
	my $doc;							# Variable for the parser object instance. 
	eval   							# Catches any parse failures and handles them gracefully.
	{
		$doc = $parser->parsefile ($infile);     	# Parse the xmlfile. 
	};
	if ($@)							# Catch XML parser errors.
	{
		print "\nParse Failure: Invalid XML in file: $infile\n\n";	
									# Print out an error if we cannot parse the XML..
		print "Parse failed with error: $@\n" if $DEBUG;		
									# XML::DOM parser error message if debug is enabled.
		$dbh->disconnect;					# Close the database handle.
		return;						# Return.
	}

	my $uid = ( split '/', $infile )[ -1 ];		# Drop the directory from the filename

	my $root = $doc->getDocumentElement();		# Get the XML document root.		
	my $filever = $root->getNodeName();			# filever is the document root nodename, should be zaproxyrun.

	# Check to see if we have a ZAP report, and not just XML
	if ($filever ne "report") 				# If it is not report then return
	{ 
		print "\n$infile does not appear to be a valid ZAP results file. Not processsing.\n\n"; 
									# Error message. 
		$dbh->disconnect;					# Close the database handle.
		$doc->dispose;                            # Clean up memory
		return;						# Return. 
	};

	my $sth_rid = $dbh->selectrow_hashref("SELECT max(reportid) as maxid FROM reports");		
									# SQL to select the last reportid.
	# Set the report identifier, one higher than last one in the database, or set it to 1.
	my $reportid;						# Variable for the report number.
	if ($sth_rid->{"maxid"}) 				# If there is a previous reportid, set the new one to one greater.
	{       
		$reportid = $sth_rid->{"maxid"} + 1;
	}								# End of if
	else 
	{
		$reportid = 1;					# Otherwise set it to 1.
	};
	# At this point the database is there and we have a valid XML file to process. 
	print "\nImporting ZAP XML file \"$infile\" into \"$dbname\" database, report $reportid\n\n" unless $quiet;	
									# Print status message

	# Database statement handles. 

	# SQL to insert the reports values:

	my $sql_reports = q{ 			
		INSERT INTO reports	  
		(reportid, filename, generated)
		values
		(?,?,?)};
	my $sth_reports = $dbh->prepare($sql_reports);

	my $sql_alerts = q{ 			
		INSERT INTO alerts	  
		(reportid, alertid, pluginid, alert, riskcode, reliability, riskdesc, description, solution, ref)
		values
		(?,?,?,?,?,?,?,?,?,?)};
	my $sth_alerts = $dbh->prepare($sql_alerts);

	my $sql_uri = q{ 			
		INSERT INTO uri	  
		(reportid, alertid, uriid, uri, param, otherinfo)
		values
		(?,?,?,?,?,?)};
	my $sth_uri = $dbh->prepare($sql_uri);

	# End of database statement handles

	# Associative arrays for contents to be inserted into database tables. 
	my %reports;						# Array to hold report elements and attributes values. 
	my %alerts;							# Array to hold alertitem elements and values.
	my @uri;							# Array to hold uri.
	my @param;							# Array to hold param.
	my @otherinfo;						# Array to hold otherinfo.

	# Loop through each of the properties for the scan (attributes of zaproxyrun document root from above).
	 
	my @nodes1;							# Array to hold getElementsByTagName results.
	my @nodes2;
	my @nodes3;
	my $attr;							# Variable for attributes of each element results. 
	my %items;							# Associative array to hold attribute names and values.
	my $item1;							# Variable for individual item. 
	my $item2;
	my $item3;
	my $element;

	if ($item1 = $root->getFirstChild->getData)					# Grab the root data, 'Report generated...'.
	{
		$reports{generated} = $item1;
	};
	$sth_reports->execute($reportid, $uid, $reports{generated});
	my $alertcount = 0;									# Alertid counter.
	# Loop to cycle through all alertitem for the report
	if (@nodes1 = $doc->getElementsByTagName("alertitem"))	
	{												# If alertitem exist parse them.
		foreach $item1 (@nodes1)							# Loop for alertitem.
		{
			for $element (qw{pluginid alert riskcode reliability riskdesc desc solution reference}) 
			{										# Loop through the elements. 
				if (@nodes2 = $item1->getElementsByTagName("$element")) 
				{									# If $element exist parse it. 
					if (defined($nodes2[0]->getFirstChild))
					{
						if ($item2 = $nodes2[0]->getFirstChild->getData)
						{							# If there is data get it and place in array.
							$alerts{$element} = $item2;
						};							# End of if data. 
					};
				};									# End of if $element.
			};										# End of elements loop.

			$alertcount++;								# Increment the counter.
			$sth_alerts->execute($reportid, $alertcount, $alerts{pluginid}, $alerts{alert}, $alerts{riskcode}, $alerts{reliability}, 
			$alerts{riskdesc}, $alerts{desc}, $alerts{solution}, $alerts{reference});
													# Insert the alerts values into the db. 
			my $counter = 0;
			if (@nodes2 = $item1->getElementsByTagName("uri")) 
			{										# If uri exist parse them.
				foreach $item2 (@nodes2)					# Loop for uri.
				{
					if (defined($item2->getFirstChild))
					{				
						if ($item3 = $item2->getFirstChild->getData)
						{							# If there is data get it and place in array.
							$uri[$counter] = $item3;
							$counter++; 				# Increment the counter 
						};							# End of if data
					};
				};									# End of uri loop.
			};										# End of if uri.
			my $counturi = $counter-1;

			$counter = 0;
			if (@nodes2 = $item1->getElementsByTagName("param")) 
			{										# If param exist parse them.
				foreach $item2 (@nodes2)					# Loop for param.
				{
					if (defined($item2->getFirstChild))
					{				
						if ($item3 = $item2->getFirstChild->getData)
						{							# If there is data get it and place in array.
							$param[$counter] = $item3;
							$counter++; 				# Increment the counter. 
						};							# End of if data.
					}
					else
					{
						$param[$counter] = " ";				# Add a blank space to the array if no data.
						$counter++;						# Increment the counter. 
					};
				};									# End of param loop.
			};										# End of if param.

			$counter = 0;
			if (@nodes2 = $item1->getElementsByTagName("otherinfo")) 
			{										# If otherinfo exist parse them.
				foreach $item2 (@nodes2)					# Loop for otherinfo.
				{
					if (defined($item2->getFirstChild))
					{				
						if ($item3 = $item2->getFirstChild->getData)
						{							# If there is data get it and place in array.
							$otherinfo[$counter] = $item3;
							$counter++; 				# Increment the counter 
						}							# End of if data
					}
					else
					{
						$otherinfo[$counter] = " ";			# Add a blank space to the array if no data.
						$counter++;						# Increment the counter. 
					};
				};									# End of otherinfo loop.
			};										# End of if otherinfo.

			my $count;									# Define a counter.
			for ($count=0;$count<=$counturi;$count++)				# Loop through each uri.
			{
				$sth_uri->execute($reportid, $alertcount, $count+1, $uri[$count], $param[$count], $otherinfo[$count]);
													# uri, param, and otherinfo into the db. 				
			};										# End of uri loop.
		};											# End of alertitem loop.
	};												# End of if alertitem.			
 

}; 													# End of process file subroutine. 

sub init_db 
{
	# Subroutine to create and initialize the database schema	
	# Setup the database connection
	my $dsn = "DBI:mysql:host=$dbhost;port=$dbport"; 	
	# Database Source Name.
	my $dbh = DBI->connect($dsn, $dbuser, $dbpass, {'RaiseError' => 1, 'AutoCommit' => 0}) 
		or die("\nERR:  Could not connect: $DBI::errstr\n" ); 
	# DataBase Handle

	# Create the database and the tables: reports, tasks, hosts, ports, scripts, os, and metrics. 
	my $idtype = "INTEGER";

	eval {
        	# Create the database if it does not exist, assuming we have privileges
        	$dbh->do("CREATE DATABASE IF NOT EXISTS $dbname");
        	print "\nCreating zaproxy database:\t$dbname\n" unless $quiet;

		# Use the database
		$dbh->do("use $dbname");

        	print "Building table:\tReports...\n" unless $quiet;
        	# Create reports table if it doesn't exist
        	$dbh->do(
            	"CREATE TABLE IF NOT EXISTS reports (
			reportid $idtype,
			filename TEXT,
			generated TEXT
           		)"
        	); # End of create for reports table

        	print "Building table:\tAlerts...\n" unless $quiet;
        	# Create alerts table if it doesn't exist
        	$dbh->do(
            	"CREATE TABLE IF NOT EXISTS alerts (
			reportid $idtype,
			alertid $idtype,
			pluginid TEXT,
			alert TEXT,
			riskcode TEXT,
			reliability TEXT,
			riskdesc TEXT,
			description TEXT,
			solution TEXT,
			ref TEXT
           		)"
        	); # End of create for alerts table

        	print "Building table:\tURI...\n" unless $quiet;
        	# Create uri table if it doesn't exist
        	$dbh->do(
            	"CREATE TABLE IF NOT EXISTS uri (
			reportid $idtype,
			alertid $idtype,
			uriid TEXT,
			uri TEXT,
			param TEXT,
			otherinfo  TEXT
           		)"
        	); # End of create for uri table

         	# Everything should have worked, so commit our changes
        	$dbh->commit();
		print "Success\n\n";
	}; 	# End of main eval
	# Rollback on any errors
	if ($@)					# Catch errors from the database create eval.
	{
        	print "\n\nDatabase initialization failed!  Transaction aborted: $@\n" unless $quiet;	# Error message
        	$dbh->rollback();			# Do not commit to the database
        	$dbh->disconnect;   		# Disconnect from the database.
        	exit(1);				# Exit. 
	};
    	$dbh->disconnect;   			# Disconnect from the database.
	exit(0);					# Exit.
} 							# End init_db sub

sub check_db
{
	# Subroutine to check if the database conforms to the current schema
	# Currently checks that all of the table names and columns names in each are the same.
	# Setup the database connection
	my $dsn = "DBI:mysql:database=$dbname:host=$dbhost;port=$dbport";    # Database Source Name.
	my $dbh = DBI->connect($dsn, $dbuser, $dbpass) or die("\nERR:  Could not connect: $DBI::err: $DBI::errstr\n" ); 
	# DataBase Handle
	# The table names currently in use (sorted alphabetically)
	my @stock_tables = ("alerts", "reports", "uri");
	my @db_tables;
	my $tablequery = "show tables";				# SQL to show tables names in the database
	my $tablesqlquery  = $dbh->prepare($tablequery);
	$tablesqlquery->execute;					# Run the query
	my $tablecount = 0;
	while (my @row= $tablesqlquery->fetchrow_array()) {	# Run through each of the table names and add to the array.
		$db_tables[$tablecount] = $row[0];
		$tablecount ++;
	}
	$tablesqlquery->finish;
	if ( @db_tables == 0)
	{
		print "\nDatabase Schema Error: Empty zaproxy database $dbname detected, please initialize\n\n";
		$dbh->disconnect;
		exit (1);
	}
	@db_tables = sort(@db_tables);			# Sort the names of tables in the db alphabetically
	# Check the actual db tables against current version later. 
	# Arrays for each of the table column names, sorted alphabetically. 
	my @db_reports = ("filename", "generated", "reportid");
	my @db_alerts = ("alert", "alertid", "description", "pluginid", "ref", "reliability", "reportid", "riskcode", "riskdesc", "solution");
	my @db_uri = ("alertid", "otherinfo", "param", "reportid", "uri", "uriid");
	# Two dimensional array containing all of the current table columns. 
	my @allorcolumns = (\@db_alerts, \@db_reports, \@db_uri);
	$tablecount= 0 ;
	my $db_ok = 1;
	print "\n";
	foreach ( @stock_tables ) 						# Loop for each of the tables names
	{ 
		my @columns;		
		my $names_query = "describe $_"; 				# Run the SQL 'describe' for each tables
		my $names_sqlquery  = $dbh->prepare($names_query);
		$names_sqlquery->execute;
		my $columncount = 0;
		while (my @row= $names_sqlquery->fetchrow_array()) 	# Grab each row
		{
			$columns[$columncount] = $row[0];			# Store the column name in the array
			$columncount ++;
		}
		@columns = sort(@columns);					# Sort the columns alphabetically
		if (@{$allorcolumns[$tablecount]} ~~ @columns)		# Check to see if database table columns are same as current
		{
			print "Table: $stock_tables[$tablecount] is OK\n" unless $quiet;		# Print status if table columns are OK
		}
		else
		{
			print "Table: $stock_tables[$tablecount] differs from current version\n";	# Print which table is different if not the same
			$db_ok = 0;							# Database is not OK, but keep processing tables/columns
		}
		$tablecount ++;
	};

	$dbh->disconnect;   							# Disconnect from the database.

	if ($db_ok == 0)
	{
		print "\nAt least one of the tables in $dbname do not match. Consider updating or initializing\n\n";
		exit (1);
	}
	if (@db_tables ~~ @stock_tables)					# If the tables names, or one of the columns do not match, exit. 
	{
		print "\nzaproxy database $dbname validated.\n\n" unless $quiet;
	}
	else 
	{ 
		print "\nDatabase Schema Error: Unrecognized zaproxy table structure.  Consider updating or initializing\n\n";
		exit (1);
	}

exit (0);
}; 											# End of check_db subroutine. 

__END__

=head1 NAME

parsezaproxyxml2mysql - Parses zaproxy xml files for insertion into a mysql database
by Adrien de Beaupre

=head1 SYNOPSIS

parsezaproxyxml2mysql.pl [options]

Options:
 
	-u, --user        Database username (string)
	-p, --password    Database password (string)
	-d, --database    Database name to use (string)
	-H, --host     	  Hostname or IP Address of MySQL server (string)
	-P, --port     	  Listener TCP port of MySQL server instance (numeric)
	-f, --file        ZAP XML results file (string)
	-D, --dir      	  Directory of zaproxy XML results files (string)
	-l, --list        Text file list of zaproxy XML results files (string)
	-v, --verbose     Increase output verbosity (flag)
	-q, --quiet       Only print errors (flag)
	-i, --initialize  Build initial table structure (flag)
	-c, --check       Check database layout (flag)
	-h, --help        Brief help message (flag)

Examples:

	- To initialize (create) the database: 			./parsezaproxyxml2mysql.pl -u root -p password -d zapdb -i 
	- To check the database table and column names: 	./parsezaproxyxml2mysql.pl -u zap -p s3kr3t -d zapdb -c 
	- To import a single ZAP XML report:			./parsezaproxyxml2mysql.pl -u zap -p s3kr3t -d zapdb -f zap.xml
	- To import a list of reports:				./parsezaproxyxml2mysql.pl -u zap -p s3kr3t -d zapdb -l allreports.list
	- To import a directory of reports:			./parsezaproxyxml2mysql.pl -u zap -p s3kr3t -d zapdb -D reports/current/

By Adrien de Beaupre. 

=cut



     


