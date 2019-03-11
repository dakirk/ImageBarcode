# ImageBarcode

This project creates a "barcode" from a set of images, where each bar is the average color of one image. It can optionally read the JSON file (media.json) provided by Instagram photo downloads to put them in chronological order, or sort them by other values. Currently WIP.

Note: when using media.json, be sure to select the top level folder of your Instagram download, since currently the barcode generator uses the addresses in media.json exactly. In a future version I will correct this.

If you just want the executable, the download is here: https://github.com/dakirk/ImageBarcode/releases/download/v1.3.1/ImageBarcode.jar

Using this JSON parser: https://github.com/stleary/JSON-java
