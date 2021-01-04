[![Build Status](https://travis-ci.org/riezenmark/access-log-analyzer.svg?branch=main)](https://travis-ci.org/riezenmark/access-log-analyzer)
[![codecov](https://codecov.io/gh/riezenmark/access-log-analyzer/branch/main/graph/badge.svg?token=23G634BS9Q)](https://codecov.io/gh/riezenmark/access-log-analyzer)

# Access log analyzer
A program for access log parsing and analyzing.
It gets an input stream of access log strings
and searches for time intervals when service level
of availability is less than given parameter.

## Parameters
 * `-u` - acceptable level of availability.
 A rational number greater than 0 and less than 100,
 e.g. **99.9**
 * `-t` - maximum response time.
 A positive rational number,
 e.g. **45.5**
 
## Using example
```
$ cat access.log | java -Xmx512M -jar analyze -u 99.9 -t 45
13:32:26	13:33:15	94.5
15:23:02	15:23:08	99.8
```
---
_See thread-safe version in [thread-safe branch](https://github.com/riezenmark/access-log-analyzer/tree/thread-safe)_
