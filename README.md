## ServerHTTPS
**TO DO**

0. <a href="https://github.com/Jakkins/ServerHTTPS#legenda"> Legenda </a>
1. <a href="https://github.com/Jakkins/ServerHTTPS#generate-keys"> Generate Keys </a>
    - CA's Keys
    - Server Keys
2. Use express to make an app with NodeJS
    - Handshake for HTTP over TLS 1.3 (Diffie-Hellman)
3. Try the HTTPS connection
---
#### What is the difference between .pem, .csr, .key and .crt? (26 Oct 2018)
> .pem stands for Privacy Enhanced Mail; it simply indicates a base64 encoding with header and footer lines.
> <a href="https://github.com/Jakkins/ServerHTTPS#sources"> Source </a>{1}

Ex.
```
-----BEGIN [label]-----
blablabla
-----END [label]-----
```
The "[label]" section describes the message, so it might be: 
- PRIVATE KEY
- CERTIFICATE REQUEST
- CERTIFICATE
Ex.
```
-----BEGIN PRIVATE KEY-----
blablalba
-----END PRIVATE KEY-----
```

One PEM file can contain multiple certificates

---
### Generate Keys
#### Generate CA's Keys
```shell
openssl req -x509 -newkey rsa:4096 -days 365 -keyout ca-key.pem -out ca-cert.pem
```
            req: command that primarily creates and processes certificate requests in PKCS#10 format.
        -newkey: create a new certificate request and a new private key
     ca-key.pem: contains the private key (encrypted)
    ca-cert.pem: contains the public key encoded in base64 (not encrypted)

#### How To Automate
```shell
openssl req -x509 -newkey rsa:4096 -days 365 -keyout ca-key.pem -out ca-cert.pem -subj "/C=IT/ST=..."
```
> -subj args

> ex. -subj "/type0=value0/type1=value1/type2=..."; characters may be escaped by ‘\’ (backslash); no spaces are skipped

#### Print The Self-Signed Certificate in a Text Format
```shell
openssl x509 -in ca-cert.pem -noout -text
```
- Country Code [C]
- State of Province Name [ST]
- Locality Name [L]
- Organization Name [O]
- Organizational Unit Name [OU]
- Common Name / Domain Name [CN] = Common Name / Email
- Email Address ⇑

#### Generate Web Server Keys
```shell
openssl req -newkey rsa:4096 -keyout server-key.pem -out server-req.pem
OR
openssl req -newkey rsa:4096 -keyout server-key.pem -out server-req.pem -subj "Change this information with web server information"
```

#### Use CA's private key to sign web server's CSR to get the signed certificate
```shell
openssl x509 -req -in server-req.pem -days 60 -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem
```
               x509: is a multi-purpose certificate utility, It can be used to sign certificate requests like a "mini CA"
               -req: expect a certificate request on input instead of a certificate
           -CA file: the CA certificate to be used for signing. When this option is present, x509 behaves like a mini CA. The input file is signed by the CA using this option
        -CAkey file: set the CA private key to sign a certificate with. Otherwise it is assumed that the CA private key is present in the CA certificate file
    -CAcreateserial: create the CA serial number file if it does not exist instead of generating an error. The file will contain the serial number ‘02’ and the certificate being signed will have ‘1’ as its serial number

#### Print The Signed Certificate Of The Web Server In A Text Format
```shell
openssl x509 -in server-cert.pem -noout -text
```

#### Subject Alternative Name (SAN) Extension
```shell
touch server-ext.cnf
```
for example write in server-ext.cnf:
```
subjectAltName=DNS:*.example.com,DNS:*.example.org,IP:0.0.0.0
```
re-sign the server request and then read the information inside the new server-cert.pem
```shell
openssl x509 -req -in server-req.pem -days 60 -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem -extfile server.ext.cnf
then
openssl x509 -in server-cert.pem -noout -text
```
There'll be a new extension section

#### To Test Without Encrypt The Private Key (PK NOT ENCRYPTED)
```shell
openssl req -x509 -newkey rsa:4096 -days 365 -nodes -keyout ca-key.pem -out ca-cert.pem
openssl req -newkey rsa:4096 -nodes -keyout server-key.pem -out server-req.pem
```
#### Verify If A Certificate Is Valid
```shell
openssl verify -CAfile ca-cert.pem server-cert.pem
```

### Create Server
#### Handshake
- what client support
- server send certificate (Public Key)
- Blowfish
- With TLS 1.2 clint encrypt the simmetric key and send it to the server ???
- Diffie Hellman (TLS 1.3)
    The client generates two keys (numbers) one public one private and merge them through complicated math so that they cannot be unmerged.
    - n is very big (2048, 4096)
- CCS attack (TLS 1.2)
- AES
- RSA Handshake
- DH 1.2 Handshake
#### TLS 1.3 (RFC 8446)
##### DH 1.3 handshake

The problem here is not so much with CBC, but with alternatives that are easier to implement safely, without losing mathematical security.In fact, AES-CBC turned out to be notoriously difficult to implement correctly. I recall that older implementations of transport layer security don't have cryptographically secure initialization vectors, which are a must-have for CBC mode

TLS 1.3 library today: BoringSSL, OpenSSL, WolfSSL implement TLS 1.3. (2018-08)
LibreSSL supports AEAD ciphers, including aes-256-gcm (2019-12)

---

openssl genrsa -aes128 -passout pass:foobar 3072

A better alternative is to write the passphrase into a temporary file that is protected with file permissions, and specify that:
openssl genrsa -aes128 -passout file:passphrase.txt 3072

Or supply the passphrase on standard input:
openssl genrsa -aes128 -passout stdin 3072

You can also used a named pipe with the file: option, or a file descriptor.

THEN

To then obtain the matching public key, you need to use openssl rsa, supplying the same passphrase with the -passin parameter as was used to encrypt the private key:

openssl rsa -passin file:passphrase.txt -pubout

(This expects the encrypted private key on standard input - you can instead read it from a file using -in <file>)

---

Example of creating a 3072-bit private and public key pair in files, with the private key pair encrypted with password foobar:

openssl genrsa -aes128 -passout pass:foobar -out privkey.pem 3072
openssl rsa -in privkey.pem -passin pass:foobar -pubout -out privkey.pub



---


---

### Sources 
##### [ sources tagged with {*} are recommended ]
##### [ sources tagged with {!} are for visual learner ]
##### [ sources tagged with {M} are for Math theory ]
- {1} <a href="https://crypto.stackexchange.com/questions/43697/what-is-the-difference-between-pem-csr-key-and-crt" target="_blank"> Common format </a>
- <a href="https://www.youtube.com/watch?v=8ptiZlO7ROs" target="_blank"> Easy start https server - YouTube (2016) </a>
- <a href="https://www.youtube.com/watch?v=7YgaZIFn7mY" target="_blank"> Create & sign SSL/TLS certificates with openssl - YouTube (2020) </a>
- <a href="https://www.youtube.com/watch?v=-f4Gbk-U758" target="_blank"> A complete overview of SSL/TLS and its cryptographic system - YouTube (2020) </a>
- <a href="https://www.libressl.org/" target="_blank"> LibreSSL </a>
- <a href="https://man.openbsd.org/openssl.1" target="_blank"> OpenSSL manual page </a>
- <a href="https://man.openbsd.org/openssl.1#req" target="_blank"> OpenSSL manual page ( REQ )</a>
- <a href="https://en.wikipedia.org/wiki/X.509" target="_blank"> X.509 </a>
- <a href="https://en.wikipedia.org/wiki/Subject_Alternative_Name" target="_blank"> Subject Alternative Name (SAN) </a>
- {!} <a href="https://www.youtube.com/watch?v=AlE5X1NlHgg" target="_blank"> Transport Layer Security, TLS 1.2 and 1.3 (Explained by Example) </a>
- {!} <a href="https://www.youtube.com/watch?v=ntytZy3i-Jo" target="_blank"> Transport Layer Security 1.3 Explained - TLS Handshake, Key Exchange, TLS Extensions and MITM </a>
- {!*} <a href="https://www.youtube.com/watch?v=NmM9HA2MQGI" target="_blank"> Secret Key Exchange (Diffie-Hellman) - Computerphile </a>
- {M} <a href="https://www.youtube.com/watch?v=Yjrfm_oRO0w" target="_blank"> Diffie Hellman -the Mathematics bit- Computerphile </a>


---

#### Legenda:
- In Public Key Infrastructure (PKI) systems
    a Certificate Signing Request (also CSR or certification request) is a message sent from an applicant to a Certificate Authority (CA) in order to apply for a digital identity certificate. The most common format for CSRs is the PKCS#10 specification. Another is the Signed Public Key and Challenge SPKAC format generated by some web browsers.
- PKCS stands for "Public Key Cryptography Standards"
- Certification Authority (CA)
    The format of these certificates is specified by the X.509 or EMV standard.
    The client uses the CA certificate to authenticate the CA signature on the server certificate, as part of the authorizations before launching a secure connection.
    Any site using self-signed certificates acts as its own CA.
- X.509
    standard for defining the format of public key certificates