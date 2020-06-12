# radiologyCTviewer

Simple GPU based ct viewer that allows scrolling through up to 20 different CT slide sets or still images, as well as means to record participant behavior.

Demo can be found at: https://thedecimeter.github.io/radiologyCTviewer/

Minimal work with JavaScript, HTML, and PHP should be expected.

Because this viewer was designed for research, it has a few differences 
from other browser based solutions (eg radiopedia):
- Doesn't display until everything is loaded. This allows for uniform timing (no waiting for images to load as they are being scrolled to).
- Provides a convenient means of logging actions
  - Clicks
  - Scrolls
  - Shader creation and implementation
  - Custom messages can be injected into any of these standard logs.
- Provides functions to check timing (note that timing on web browsers/JavaScript vary wildly, so super percise timing shouldn't be relied upon)
- Provides many forms of input to mimmic the expectations of participants.
  - Scroll wheel
  - Dragging
  - Keyboard
- Set display size to be in pixels, inchs, or through a custom method.
  - Leave the scaling to the viewer. The coordinates themselves mimmic a specific resolution you define regardless of the displayed resolution.
- Apply custom glsl shaders (if they are compatible with webGL).
  - Simple functions provided to create shaders which constrain window level/width (like common dicom viewers)
  - Because the viewer runs on the GPU, image manipulation feels instantanious.
- Add custom shapes to the image or the UI for search tasks or debriefing.
- Supports weaker platforms
  - chops up large images into textures which can fit on the client's GPU
  - splits long initializing tasks so that browsers don't complain about unresponsive JavaScript.
  - allows faking smaller GPU texture size so that you can test performance of other platforms.

Provided as-is for any use you wish.

Written by Daniel Williams (http://www.curiousorigins.com/)

Powered by libGDX (https://libgdx.badlogicgames.com/) under the Apache 2.0 license (https://www.apache.org/licenses/LICENSE-2.0)
