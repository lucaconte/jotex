# Introduction #




# Details #

Simply,
  * download binary distribution
```
            jotex<VERSION>.jar
```
  * open a Terminal or a DOS prompt
  * Go to download directory and type:
```
            java -jar jotex<VERSION>.jar <FILENAME>.odt
```
When the process will be terminated you'll find
```
            <FILENAME>.epub
```
> in the same original odt's directory.

Options:
```

usage: java -jar jotex<VERSION>.jar [<OPTIONS>] <FILE_NAME>.odt
 -b                            Expose bookmarks (if presents) in
                               alphabetical order,  at the end of the TOC
 -c,--cover <COVER_IMG_FILE>   Full path to the epub cover (gif or jpg or
                               png). If not present no cover will be used
 -d                            Enable debug mode
 -e,--fem <adobe|idpf>         Fonts Encriptyon Method. Used if, for
                               licensing reasons, fonts need to be
                               encrypted.
 -f,--fp <FONTS_PATH>          Fonts Path. A path in which Jotex looks for
                               Fonts files that need to be embedded (in an
                               encrypted form [see "fem" option]) into the
                               epub.
                               Without this param NO FONT will be included
                               into the epub
 -h,--help                     Print Jotex comman line help
 -o,--output <OUTPUT_FILE>     Output file. If not present, Jotex will
                               create an epub file in the same original
                               odt's directory  with the same original
                               odt's file name
 -s,--max-file-size <SIZE>     Max single xhtml's file size in KB

```