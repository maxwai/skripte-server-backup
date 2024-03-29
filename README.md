![Java](https://badgen.net/badge/language/Java/green)
![Java](https://badgen.net/badge/Java/JDK-17/green)
[![GitHub license](https://badgen.net/github/license/maxwai/skripte-server-backup)](LICENSE)

# Skripte Server Backup Tool

The source code that I use to make a periodic backup of the Script Server at the HM.

## Getting Started

### Prerequisites

You will need Java Version 17 or later to make it work. It may work with lower Java versions, but it
was programmed using the Java 17 JDK.

### Needed environment variables
 - `WEBSITE` : The URL to the HM Script Server Website
 - `SAVE_PATH` : The Path where the Script Server should be saved to
 - `INTERVAL` : The Interval on which the Script Server should be crawled.  
   possibilities (\<x\> being a number):
   - `<x>d` : the interval is given in days
   - `<x>h` or `<x>` : the interval is given in hours
   - nothing less than 1 hour is allowed since this would be too much traffic to the Website
 - `DEBUG` - Optional : `true` or `false` for debug output. Default: `false`