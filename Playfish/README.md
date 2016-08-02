HTML Output:
- https://htmlpreview.github.io/?https://github.com/JamesMcGuigan/recruitment-technical-tests/blob/master/Playfish/Simple/Playfish.html
- https://htmlpreview.github.io/?https://github.com/JamesMcGuigan/recruitment-technical-tests/blob/master/Playfish/Extended/PlayfishLikeButton.html


My solution to the problem posed is attached.

1. The game should be able to display and hide a button at any time.
No buttons are initially visible.
-- likeButton.render() // not called by constructor
-- likeButton.show() // must call render first, if you want it visible
-- likeButton.hide() // can call before render() to hide on render


2. The URL to be ‘Liked’ will be supplied by the game when a button is shown.
-- likeButton = new Playfish.LikeButton("flashcontent", { action:
"Unlike", href: "http://apps.facebook.com/crazyplanets/?pf_ref=sb" }
);

3. The game must be able to set the position of the button at any time
to allow for animation. Position will be specified in pixel
coordinates relative to the top-left corner of the Flash application
(not the page).
-- likeButton.setPosition(100, 100)

4. Code structure should be Object Oriented.
-- Its a function returning a hash of first class functions... and we
have thrown in the keyword new. Well close enough.

5. No third party libraries are to be used.
-- Technically firebugx.js is a library, included inline, but its only
purpose is to stop IE from complaining about console.log()


1. Assume no cross-domain scripting restrictions.
-- Originally tried implementing this as a bookmarklet, the playfish
iFrame is served from playfish servers, but it insists on loading via
a facebook page. Cross-domain scripting restrictions won't allow a
bookmarklet or even firebug console code to bypass this restriction.
Hosted my own HTML page to get round this restriction.


For extra credit:
- Support display of multiple buttons simultaneously
-- Create new instance of Playfish.LikeButton for each button you wish to create

- A JavaScript-only solution (no static HTML)
-- None required, but js must be loaded from same server as the html page

- Structure the code to minimise naming clashes with third-party
scripts embedded on the same page
-- Assumes Playfish.* is the company wide class namespace
-- Script Init code in a private namespace

- The module will typically be used to add ‘Like’ buttons to popup
dialog boxes displayed in the Flash application. Discuss any potential
pitfalls that may arise.

- Major issue would be browser text size, which is potentially user
controlled and browser dependant. Text size can also be affect by user
and site stylesheets, that may also not be under the control of the
flash author. The result is that the text may become larger than the
popup box. A custom stylesheet to override the default facebook CSS
for the like button (setting a max-width property for example) might
limit some of the potential damage.

- You would also need to make sure that the flash hasn't been assigned a
greater z-index than the text. As this would make the text a little
hard to see and to click.

Using OOP, include support for displaying a div containing text
rather than the ‘Like’ button.

- Not attempted. Couldn't find any online documentation on the API
for this (I assume thats the point). Would create an alternate code
path within buildElement to render a div rather than an iFrame (if
passed a parameter). Then would create a javascript click handler to
attempt to clone the observed ajax call, then parse the returned json
and update the link.


Issues:
- Rendering of Like Button in IE6 is ugly, not quite sure why, may be
an issue with the default Facebook stylesheet

--
Jamie
