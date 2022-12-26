[![Release](https://jitpack.io/v/umjammer/jlayer.svg)](https://jitpack.io/#umjammer/jlayer)
[![Java CI](https://github.com/umjammer/jlayer/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/jlayer/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/jlayer/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/jlayer/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)
[![Parent](https://img.shields.io/badge/Parent-mp3spi-pink)](https://github.com/umjammer/mp3spi)

# JLayer

 JavaZOOM 1999-2008

 Project Homepage :<br/>
   [http://www.javazoom.net/javalayer/javalayer.html](https://web.archive.org/web/20210108055829/http://www.javazoom.net/javalayer/javalayer.html) 

 JAVA and MP3 online Forums :<br/>
   [http://www.javazoom.net/services/forums/index.jsp](https://web.archive.org/web/20041010053627/http://www.javazoom.net/services/forums/index.jsp)

----

## DESCRIPTION

JLayer is a library that decodes/plays/converts MPEG 1/2/2.5 Layer 1/2/3
(i.e. MP3) in real time for the JAVA(tm) platform. This is a non-commercial project 
and anyone can add his contribution. JLayer is licensed under LGPL (see [LICENSE](LICENSE.txt)).


## FAQ

### How to install JLayer ?

 * https://jitpack.io/#umjammer/jlayer

### Do I need JMF to run JLayer player ?

  No, JMF is not required. You just need a JVM JavaSound 1.0 compliant.
  (i.e. JVM1.3 or higher).

### How to run the MP3TOWAV converter ?

```
  java javazoom.jl.converter.jlc -v -p output.wav yourfile.mp3
```

  (Note : MP3TOWAV converter should work under jdk1.1.x or higher)

### How to run the simple MP3 player ?

```
  java javazoom.jl.player.jlp localfile.mp3
```

   or

```
  java javazoom.jl.player.jlp -url http://www.aserver.com/remotefile.mp3
```

  Note : MP3 simple player only works under JVM that supports JavaSound 1.0 (i.e JDK1.3.x+)

### How to run the advanced (threaded) MP3 player ?

```
  java javazoom.jl.player.advanced.jlap localfile.mp3
```

### Does simple MP3 player support streaming ?

  Yes, use the following command to play music from stream :

```
  java javazoom.jl.player.jlp -url http://www.shoutcastserver.com:8000
```

  (If JLayer returns without playing SHOUTcast stream then it might mean 
   that the server expect a winamp like `"User-Agent"` in HTTP request).

### Does JLayer support MPEG 2.5 ?

  Yes, it works fine for all files generated with LAME.

### Does JLayer support VBR ?

  Yes, It supports VBRI and XING VBR header too. 

### How to get ID3v1 or ID3v2 tags from JLayer API ?

  The API provides a `getRawID3v2()` method to get an `InputStream` on ID3v2 frames.

### How to skip frames to have a seek feature ?

  See `javazoom.jl.player.advanced.jlap` source to learn how to skip frames.
