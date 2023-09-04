#!/usr/bin/perl
# 
# For icns, requires software iconutil on a Mac.
#
# If you want to re-code this script to generate icns with Perl's
# image libraries, go for it.  If you want to write in another
# language, again, go for it.
#
# Requires ImageMagick to be installed if you want to create
# Windows ico.
#
# Copyright 2014 Aspect Security - rights assigned to the OWASP ZAP project
# Copyright 2014 The ZAP Development Team
# 

use File::Copy qw(copy);
use File::Remove qw(remove);
use File::Glob qw(bsd_glob);

@square_sizes = (
	16, 32, 64, 128, 256, 512, 1024, # the standard sizes of .png files.
);

$base="zap";     # basename of png files, e.g.; zap16x16_bw.png --> zap
$suffix="";      # suffix of png files, e.g.; zap16x16_bw.png --> _bw

$final_icns_name="ZAP";  # ZAP in caps
$final_ico_name="zap";   # ZAP in lower case

$dir="tmp.iconset";
mkdir $dir;

@square_sizes = ( 16, 32, 64, 128, 256, 512, 1024);
while ( $i = shift @square_sizes ) {
	#$orig = sprintf "%s%d%s.png", $base, $i, $suffix;  # e.g.: zap16bw.png
	$orig = sprintf "%s%dx%d%s.png", $base, $i, $i, $suffix;
	$new = sprintf "%s/icon_%dx%d.png", $dir, $i, $i;
	copy $orig, $new;
	$new = sprintf "%s/icon_%dx%d\@2x.png", $dir, $i >> 1, $i >> 1;
	copy $orig, $new;
}


# remove non-standard sizes, only allowed 10 images in icns
unlink "$dir/icon_8x8\@2x.png" , "$dir/icon_64x64.png" , "$dir/icon_64x64\@2x.png" , "$dir/icon_1024x1024.png";

system "iconutil", ("-c", "icns", "$dir");
rename "tmp.icns" , "$final_icns_name.icns";
remove \1, "$dir";



exit 0;


# The code below generates an ico file, but for some reason, the
# background is not transparent.  There is a known bug in some versions
# of ImageMagick where channel gets munged.  Maybe that is the issue,
# but the work-around didn't fix it.  No matter, we manually created the
# new and pretty ico file from http://iconverticons.com/online/ instead.
# So, use and fix this code if you really want for other ico uses
# without resorting to a separate website.


# Possible bug fix: "-channel", "Alpha", "-negate",

# Make ico
@args = ( );
@square_sizes = ( 16, 32, 64, 128, 256 );
$orig = sprintf "%s%dx%d%s.png", $base, 1024, 1024, $suffix;
push @args, "$orig", "-bordercolor", "white", "-border", "0";
while ( $i = shift @square_sizes ) {
	$s = sprintf "%dx%d", $i, $i;
	push @args, "(", "-clone", "0", "-resize", $s, ")";
}
push @args, "-delete", "0", "-colors", "65536", "-channel", "Alpha", "-negate", "$final_ico_name.ico";
system "convert", @args;






# This code might be useful for a future icon, but we leave it commented out.
# Basically, the -clone option of ImageMagick will resize the images for us,
# but we did the export in PhotoShop already, so there is no need.


# Vista 65526 color 256 pixel icos
@args = ( );
@square_sizes = ( 16, 32, 64, 128, 256 );
push @args, "base_square.png",  "-bordercolor", "white", "-border", "0";
while ( $i = shift @square_sizes ) {
	$s = sprintf "%dx%d", $i, $i;
	push @args, "(", "-clone", "0", "-resize", $s, ")";
}
push @args, "-delete", "0", "-alpha", "off", "-colors", "65536", "256.ico";
system "convert", @args;



=head
This is the ico creator - see ImageMagick recipes.
 convert base_square.png  -bordercolor white -border 0 \
           \( -clone 0 -resize 16x16 \) \
           \( -clone 0 -resize 32x32 \) \
           \( -clone 0 -resize 48x48 \) \
           \( -clone 0 -resize 64x64 \) \
           -delete 0 -alpha off -colors 256 64.ico


# XP old school 64 pixel icons
@args = ( );
@square_sizes = ( 16, 32, 48, 64, );
push @args, "base_square.png",  "-bordercolor", "white", "-border", "0";
while ( $i = shift @square_sizes ) {
	$s = sprintf "%dx%d", $i, $i;
	push @args, "(", "-clone", "0", "-resize", $s, ")";
}
push @args, "-delete", "0", "-alpha", "off", "-colors", "256", "64.ico";
system "convert", @args;

=cut
