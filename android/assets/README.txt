_____ACCESS FUNCTIONS____

To access the viewer from HTML there are a few JavaScript methods you can call:

 - pipeInput() This is used to help browser compatibility as well as let keyboard input be used even if the viewer isn’t the focused element on the page (convenient for if a user is filling out a form and quickly uses the arrows to scroll up/down. Note that you may wish to disable WASD scrolling for text boxes (or make a rule in javascript that doesn’t pipe W and S if a text box has focus).

 - getLastClick() use this to get an x, y, slice, and time of the last click in the viewer (if the current slice is the gray “no slices” then slice will be 1). If overwriteLastClick (in the config file) is false, then this will return the first click after the last call to resetLastClick().

 - resetLastClick() set the last click to 0.

 - getUpTime() get the time in milliseconds since the program started (this start point is well after the page loads depending on image sizes. These times aren’t as accurate as what you are probably used to (JavaScript just isn’t as good in this area). This is the same timer used for click logging

 - getStartTime() when the program starts it calls the javascript "Date.now()" useful if you want to see what time of day the user participates as well as a base point for how long it takes your images to load.

 - setMode(integer: mode) set the viewer to display a specific file, 0-20 (inclusive). If it is 0, the gray screen will be displayed. The slice will be the same slice shown the last time this mode was displayed (or the first if it was never displayed before).

 - setModeAt(integer: mode, integer: slice) same as setMode but also lets you specify a specific slice to start at (1 – number of slices, with an index of 0 this function behaves the same as setMode).

 - getClicksFor(integer: mode, string: component partition, string, item partition) get a string with all the clicks for a particular slide (specified by the first parameter). The second two strings specify how you wish to divide the information, for a csv use: getClicksFor(index, “,”, “\n”) where index is the mode you are interested in.




____VIEWER CONFIG_____

there is a config.ini in the asset folder

---Global---
 - fakeDensity: should density be “faked” or real. If faked, the viewer will try to display at the same size in inches on different displays (this uses Android’s convention of emulating a pixel on a 160 dpi screen). If not faked, the viewer will have the same pixel count on every system. Note, it is not uncommon for users to scale their browser windows manually so neither size nor pixel count can be guaranteed.


---Window---
 - simple self explanatory width and height pixel values here. Uses density independent pixels (dips) when faking density.


---Click---
 - radius: the size of the click marker in pixels or dips.
 - depth: the number of slices above and below a click to show a marker (0 only shows up on the slice which was clicked).
 - color: the hex color in RGBA for the click marker

---Controls---
 - wheel: use the scroll wheel to traverse the image
 - arrows: use the Up and Down keys to traverse the image
 - wasd: use the W and S keys to traverse the image
 - holdTime: how long do you hold the key before the slide advances (in seconds)
 - drag: use the touch screen or mouse to traverse the image
 - dragDist: how far is the finger/cursor dragged before the slide advances (in pixels or dips)

---Debug---
 - advanceSlide: press the M key when the viewer is in focus to move to the next slide set (for actual use this should be disabled and setMode / setModeAt should be used).





____SLIDE CONFIG_____

there is a slidedim.txt with slide specific settings on it

Each setting is a list of values in this order:
 - File Number
 - How many slices wide is the image (integer)
 - How many slices tall is the image (integer)
 - How many slices are in the image (integer)
 - Are clicks logged on these slices (t, true, f, false)
 - the file extension for the image. (this is specified twice in another file as well, all three are required).


____ASSETS_____

There is a slides folder with 20 images in it. Keep the file names the same (with the exception of the extension which is declared in the above file)

If you change the extension (and I recommend you use jpeg) you must also change it for the appropriate file in the assets.txt file (twice, and note that the mime type for jpg is "jpeg")".