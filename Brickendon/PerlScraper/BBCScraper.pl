#!/usr/bin/perl
use strict;
use LWP::Simple;
use URI::URL;
use Thread qw/async yield/;

my @urls     = ();
my @words    = ();
my @excludes = ();

foreach (@ARGV) {
    if    (m!^\w+://!) { push( @urls,     $_); }
    elsif (m!^-!)      { push( @excludes, $_); }
    else               { push( @words,    $_); } 
}

if( !@urls && !@words ) {
    print "Usage: BBCScraper.pl [urls] [words] [-excludes]";
    exit;
}
if( !@urls ) { push(@urls, "http://www.bbc.co.uk/news/"); }

my @threads = ();
foreach my $url (@urls) { 
    push( @threads, async {
        my $html = get $url or die "Unable to fetch $url"; 

        my ($href, $headline, $summary) = ($html =~ m{
            <div[^>]*id="[^">]*top-story[^>]*>
                .*?
                <h2[^>]*class="[^">]*top-story-header[^>]*>
                (?:
                    \s+                                    # ignore starting whitespace
                    |<span.*?</span>                       # ignore spans
                    |<ul.*?</ul>                           # ul ignore (used for see-also)
                    |<img[^>]*/?>
                    |</a>
                    |<a[^>]*href="([^">]*)"[^>]*>          # $1 = href
                    |([^<>]+)                              # $2 = headline text
                    |.*?                                   # failsafe
                )*?
                </h2>
                .*? 
                <p>([^<]*)                                 # $3 = summary text
                .*?
            </div>
        }six);
        ($href, $headline, $summary) =~ s/^\s+|\s+$//g;
        $href = url($href)->abs($url)->as_string; 

        print "Main Story URL:      $url\n";
        print "Main Story Headline: $headline\n";
        print "Main Story Summary:  $summary\n";
        print "Main Story href:     $href\n";
                                
        if( @urls ) {
            my $paragraph_regexp = '<(?:a|p|span|h2|h3)[^>]*>([^<>]*\b(?:'.join('|',@words).')\b[^<>]*)<[^>]*>';
            my $exclude_regexp   = '\b(?:'.join('|', map(/^-?(.*)/, @excludes)).')\b';
            my (@paragraphs) = $html =~ m/$paragraph_regexp/i;
            @paragraphs              = grep(!/$exclude_regexp/i, @paragraphs) if(@excludes);
            
            print "paragraphs (".($#paragraphs+1)."):\n".join("\n", @paragraphs);
        }
        print "\n\n";
    }); 
}
foreach my $thread ( @threads ) { $thread->join(); }
