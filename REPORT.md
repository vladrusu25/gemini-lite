# Project Report

Author: Vlad Rusu
Email: v.rusu@student.maastrichtuniversity.nl
Student ID number: i6377598

## Gemini Lite Client Program

In order to run the client program, use the command :
```bash
java -cp target/bcs2110-2025.jar gemini_lite.Client <URL> [<input>]
```
where URL is a valid Gemini URL (e.g. gemini://example.com) and input is an optional argument used when the server requests user input.
### Example usage
```bash
java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://demo.svc.leastfixedpoint.nl/
```

### Handling "slow down" replies
As stated in the protocol specification, the client is using exponential backoff when handling "slow down"  replies. 
In my current implementation, the client prioritizes the backoff duration stated in the META header over the default backoff duration of 1 second.
If the duration is not specified in the META header, the client will use the default backoff duration. The backoff duration is doubled with each
"slow down" reply received from the same origin server, up to a maximum of 60 seconds. Whenever the origin changes, the backoff duration is reset.

### Handling input requests 
When the server requests input (replies with status code 1x), the client checks if an input argument was provided in the command line.
If so, it uses that input to respond to the server. If no input argument was provided (or the input was already used), the client
interacts with the user via the system console to get the required input. In case the status code is 11 (sensitive input),
the client calls the Console.readPassword() method to extract user input. In all cases, the client builds a new URI based on the 
current URI and the user input, and sends a new request to that URI.

### Encountering buggy/hostile servers
The client is designed to handle various types of buggy or hostile server behavior gracefully. For example, if the reply line received from the server
is malformed, misses CRLF termination, or contains an invalid status code, the ClientEngine throws a ProtocolException that is later handled in Client, which
catches it and terminates with exit code 1.

### Handling redirections
The client handles redirections (replies with status codes 3x) by building a new URI based on the current URI and the META header received from the server.
As stated in the protocol specification, the client follows up to 5 redirections per request. If the maximum number of redirections is exceeded,
the client terminates with exit code 1. On a valid redirect, the client sends a new request to the new URI.

### Bonus enhancements

No bonus enhancements were attempted for the client program.

## Gemini Lite Server Program

In order to run the client program, use the command :
```bash
java -cp target/bcs2110-2025.jar gemini_lite.Server <directory> [<port>]
```
where directory is the root directory(it must be an existing directory) from which the server will serve files. Port is an optional argument
specifying the port that the server will listen on. In case user does not specify the port, it is automatically set to 1958.
### Example usage
```bash
java -cp target/bcs2110-2025.jar gemini_lite.Server /home 1999
```
### Restricting file access outsite the root directory
The server uses a FileSystemRequestHandler to handle incoming requests. If the URI path maps to a file outside the root directory,
the server responds with status code 51 (not found).

### Handling requests for URL paths that map to directories
The FileSystemRequestHandler checks if the URI path maps to a directory. If so, it looks for an index.gmi file inside that directory.
If the index.gmi file exists, it is served to the client. If not, the server generates a Gemtext directory listing
of all files and directories inside that specific directory. 

### Dealing with hostile client behavior
The server is designed to handle various types of hostile client behavior gracefully. First of all, there is a 30 seconds timeout for each request. In case this limit is exceeded, socket throws an exception.
If the server receives a malformed request line, it responds with status code 59 (bad request). Any other unexpected exceptions are caught and the server responds with status code 50.

### Request latencies

| URL                                   | P50_ms | P99_ms |
|---------------------------------------|--------|--------|
| gemini-lite://localhost/64.bin        | 123.72 | 139.31 |
| gemini-lite://localhost/1024.bin      | 138.70 | 229.56 |
| gemini-lite://localhost/131072.bin    | 153.63 | 215.34 |
| gemini-lite://localhost/104857600.bin | 2262.04| 2314.14|

### Average throughput for 100 MB file

| URL                                   | Seconds | BytesPerSec |
|---------------------------------------|---------|-------------|
| gemini-lite://localhost/104857600.bin | 1.94    | 54108456    |

### Bonus enhancements

There were no bonus enhancements attempted for the server program.

## Gemini Lite Proxy Program

In order to run the proxy program, use the command :
```bash
java -cp target/bcs2110-2025.jar gemini_lite.Proxy <port>
```
where port is the port that the proxy will listen on.
### Example usage
```bash
java -cp target/bcs2110-2025.jar gemini_lite.Proxy 9999
```
### Implementation
The proxy implementation was possible by reusing the ClientEngine and ServerIO classes that were created initially to serve the client and the server.
A ProxyRequestHandler class was created to handle incoming requests to the proxy. 
ClientEngine was adapted to work in 2 modes: direct mode (when used by the client) and proxy mode (when used by the proxy).
In proxy mode, as stated in the protocol specification, input requests are passed back to the client.

### Proxy error handling (status code 43)
The main errors that cause the proxy to respond with status code 43 (proxy error) are: Invalid URL scheme, missing host in the target URL, requests that have
a line length that exceed 1024 bytes (protocol's maximum). In general, the protocol exceptions are the one that cause the ProxyRequestHandler to respond with status code 43.

### Bonus enhancements

There were no bonus enhancements attempted for the proxy program.


## Alternative DNS, Bakeoff and Wireshark outputs
### BIND confing 
```
zone "moldova.lab-kale" {
type primary;
file "/var/lib/bind/db.zone.lab-kale";
};
```
### Dig output
#### dig -t ns lab-kale
```angular2html
pi0050@pi0050:~ $ dig -t ns lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> -t ns lab-kale
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 15585
;; flags: qr rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 2

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: a6d8e399140df6e801000000691efc41d81b45eabf3007f0 (good)
;; QUESTION SECTION:
;lab-kale.                      IN      NS

;; ANSWER SECTION:
lab-kale.               10      IN      NS      ns1.lab-kale.

;; ADDITIONAL SECTION:
ns1.lab-kale.           10      IN      A       10.2.0.100

;; Query time: 100 msec
;; SERVER: 10.2.0.1#53(10.2.0.1) (UDP)
;; WHEN: Thu Nov 20 12:32:17 CET 2025
;; MSG SIZE  rcvd: 99

```
#### dig @10.2.0.100 telecentru.moldova.lab-kale
```angular2html
pi0050@pi0050:~ $ dig @10.2.0.100 telecentru.moldova.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> @10.2.0.100 telecentru.moldova.lab-kale
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 8993
;; flags: qr rd; QUERY: 1, ANSWER: 0, AUTHORITY: 1, ADDITIONAL: 2
;; WARNING: recursion requested but not available

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 44604fc44838382f01000000691efc47dc8f096725290a59 (good)
;; QUESTION SECTION:
;telecentru.moldova.lab-kale.   IN      A

;; AUTHORITY SECTION:
moldova.lab-kale.       10      IN      NS      ns1.moldova.lab-kale.

;; ADDITIONAL SECTION:
ns1.moldova.lab-kale.   10      IN      A       10.2.0.114

;; Query time: 4 msec
;; SERVER: 10.2.0.100#53(10.2.0.100) (UDP)
;; WHEN: Thu Nov 20 12:32:23 CET 2025
;; MSG SIZE  rcvd: 118

```
#### dig @10.2.0.100 telecentru.moldova.lab-kale
```angular2html
pi0050@pi0050:~ $ dig @10.2.0.100 telecentru.moldova.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> @10.2.0.100 telecentru.moldova.lab-kale
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 8993
;; flags: qr rd; QUERY: 1, ANSWER: 0, AUTHORITY: 1, ADDITIONAL: 2
;; WARNING: recursion requested but not available

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 44604fc44838382f01000000691efc47dc8f096725290a59 (good)
;; QUESTION SECTION:
;telecentru.moldova.lab-kale.   IN      A

;; AUTHORITY SECTION:
moldova.lab-kale.       10      IN      NS      ns1.moldova.lab-kale.

;; ADDITIONAL SECTION:
ns1.moldova.lab-kale.   10      IN      A       10.2.0.114

;; Query time: 4 msec
;; SERVER: 10.2.0.100#53(10.2.0.100) (UDP)
;; WHEN: Thu Nov 20 12:32:23 CET 2025
;; MSG SIZE  rcvd: 118

```
#### dig @10.2.0.119 telecentru.moldova.lab-kale
```angular2html
pi0050@pi0050:~ $ dig @10.2.0.119 telecentru.moldova.lab-kale

; <<>> DiG 9.18.41-1~deb12u1-Debian <<>> @10.2.0.119 telecentru.moldova.lab-kale
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 28547
;; flags: qr aa rd; QUERY: 1, ANSWER: 0, AUTHORITY: 1, ADDITIONAL: 1
;; WARNING: recursion requested but not available

;; OPT PSEUDOSECTION:
; EDNS: version: 0, flags:; udp: 1232
; COOKIE: 783e8d5169c3478501000000691efc57d05725a6e01ed0b8 (good)
;; QUESTION SECTION:
;telecentru.moldova.lab-kale.   IN      A

;; AUTHORITY SECTION:
telecentru.moldova.lab-kale. 10 IN      SOA     ns1.telecentru.moldova.lab-kale. hostmaster.telecentru.moldova.lab-kale. 1 10 10 10 10

;; Query time: 24 msec
;; SERVER: 10.2.0.119#53(10.2.0.119) (UDP)
;; WHEN: Thu Nov 20 12:32:39 CET 2025
;; MSG SIZE  rcvd: 135

```

### Lab 5: Packet capture of joining a network
```
1. Source : c0:35:32:08:37:21 
   Destination : ff:ff:ff:ff:ff:ff
```

```
2. Source : 0.0.0.0
   Destination : 255.255.255.255
```

```
3. Source : 68
   Destination : 67
```
```angular2html
4. 0.0.0.0
```
```angular2html
5. Yes, 10.2.1.229
```
```
6. Source : 2c:cf:67:32:7f:67
   Destination : c0:35:32:08:37:21
```
```
7. Source : 10.2.0.1
   Destination : 10.2.1.229
```
```
8. Source : 67
   Destination : 68
```
```angular2html
9. 10.2.1.229
```

```angular2html
10. Subnet mask : 255.255.252.0
    Router : 10.2.0.3
    DNS : 10.2.0.1
```

### Lab 5: Port Scan
```angular2html
PS C:\Users\Vlad> nmap -T5 pi0050.kale
Starting Nmap 7.98 ( https://nmap.org ) at 2025-11-27 12:04 +0100
Nmap scan report for pi0050.kale (10.2.0.114)
Host is up (0.24s latency).
Not shown: 998 closed tcp ports (reset)
PORT   STATE SERVICE
22/tcp open  ssh
53/tcp open  domain
MAC Address: 2C:CF:67:32:7C:1C (Raspberry Pi (Trading))

Nmap done: 1 IP address (1 host up) scanned in 36.41 seconds
```

```angular2html
PS C:\Users\Vlad> nmap -A -T5 pi0050.kale
Starting Nmap 7.98 ( https://nmap.org ) at 2025-11-27 12:08 +0100
Nmap scan report for pi0050.kale (10.2.0.114)
Host is up (0.072s latency).
Not shown: 998 closed tcp ports (reset)
PORT   STATE SERVICE VERSION
22/tcp open  ssh     OpenSSH 9.2p1 Debian 2+deb12u3 (protocol 2.0)
| ssh-hostkey:
|   256 72:43:52:67:78:e0:23:ed:c9:f4:ff:89:fb:4a:91:03 (ECDSA)
|_  256 28:29:a7:73:f7:82:f7:c5:56:1f:0c:8a:c0:ae:23:49 (ED25519)
53/tcp open  domain  ISC BIND 9.18.41-1~deb12u1 (Debian Linux)
| dns-nsid:
|_  bind.version: 9.18.41-1~deb12u1-Debian
MAC Address: 2C:CF:67:32:7C:1C (Raspberry Pi (Trading))
Aggressive OS guesses: Linux 2.6.32 (96%), Linux 4.15 (96%), OpenWrt 21.02 (Linux 5.4) (96%), MikroTik RouterOS 7.2 - 7.5 (Linux 5.6.3) (96%), Linux 3.2 - 4.14 (96%), Linux 4.15 - 5.19 (96%), Linux 2.6.32 - 3.10 (96%), Linux 4.19 (96%), Linux 6.0 (96%), Linux 3.4 - 3.10 (95%)
No exact OS matches for host (test conditions non-ideal).
Network Distance: 1 hop
Service Info: OS: Linux; CPE: cpe:/o:linux:linux_kernel

TRACEROUTE
HOP RTT      ADDRESS
1   71.79 ms 10.2.0.114

OS and Service detection performed. Please report any incorrect results at https://nmap.org/submit/ .
Nmap done: 1 IP address (1 host up) scanned in 50.48 seconds
```
## Reflection on Gemini Lite
### Answer Q1
I think the main reason of departing from HTTP's scheme was simplicity. Gemini has a more human-friendly response code design.
The shape of the status codes of Protocol Gemini (1x-6x) are easier to understand than HTTP's (1xx-5xx).
Having fewer status sub-codes (maximum of 10 per status class compared to HTTP's maximum of 99 per status class) also contributes to simplicity.
Even if HTTP 1.1 does not use the majority of the available status codes, having a smaller set of status codes assures that future 
versions will be as compact as possible. However, this simplicity comes at a cost: the lack of features like caching proxies, which are possible in HTTP because of the richer headers.

### Answer Q2
I think that implementing a caching proxy in Gemini Lite is almost impossible, because of the simplicity of the reply headers.
They only contain a status code and the META field, which cannot be used to determine the "freshness" of a resource.
Without a "Last-Modified" or "Expires" header, the proxy cannot determine if a cached resource is still valid or if it needs
to be requested again from the server.
### Answer Q3
The specification states that the proxy must treat upstream redirections and "slow down" in the same way the client does.
I think this design choice makes sense, because it simplifies the flow of the requests and replies : why should the
the flow be client -> proxy -> server (server replies with redirect) -> proxy -> client -> proxy -> server, when it can be
client -> proxy -> server (server replies with redirect) -> proxy -> server. The same applies for "slow down" replies.
The proxy already uses the engine used by the client, so making it responsible for handling these cases is a logical choice.
In my opinion, the specification should state that thr proxy must handle these cases, and only relay the input requests back to the client(as it currently does).

### Answer Q5
In my opinion, the simplicity of Gemini Lite reply lines makes the headers shorter and thus improves the bandwidth usage. On the other hand,
the lack of information in the reply header makes it almost impossible to implement features like caching proxies. Without features like this, 
there is a lot of redundant data being transferred over the network, so in the end I think this tradeoff does not lead to efficient bandwidth usage.
It could be improved by adding some extra headers like "Last-Modified" or "Expires", but then we have to think about why these features are not 
included in the first place: keeping the protocol as simple as possible. When it comes to reliability, I think Gemini Lite is not a very reliable application layer protocol, because of the lack of any 
integrity checks or encryption mechanisms. However, using TCP as the transport layer protocol does help with this, as it provides a checksum check for the data being transferred.