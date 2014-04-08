register_virtual_stack_slices
=============================

This plugin takes a sequence of image slices stored in a folder, and delivers a list of registered image slices (with enlarged canvas). One of the images in the sequence can be selected by the user as reference and it will remain intact.

The plugin can perform 6 types of image registration techniques:

- Translation
- Rigid (translation + rotation)
- Similarity (translation + rotation + isotropic scaling)
- Affine
- Elastic (via bUnwarpJ with cubic B-splines)
- Moving least squares

All models are aided by automatically extracted SIFT features.

For further details, visit the documentation site: http://fiji.sc/Register_Virtual_Stack_Slices
