# radiologyCTviewer

Simple GPU based ct-viewer that allows scrolling through up to 20 different CT slide sets or still images, as well as means to record
clicks and scroll behavior.

Because this viewer was designed for research, it has a few differences 
from other browser based solutions (eg radiopedia):
- No display until everything is loaded. This allows for uniform timing (no waiting for images to load as they are being scrolled to).
- Provides a convenient means of recording clicks and scrolling behavior of participants
- Provides functions to check timing of certian operations (note that JavaScript is not as good at timing as other platforms)
- Provides many forms of input (scroll wheel, dragging, keyboard) to mimmic the expectations of participants.
- Sets display size to be in pixels, inchs, or a custom method.
