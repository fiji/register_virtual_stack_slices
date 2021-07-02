[![](https://github.com/fiji/register_virtual_stack_slices/actions/workflows/build-main.yml/badge.svg)](https://github.com/fiji/register_virtual_stack_slices/actions/workflows/build-main.yml)

register_virtual_stack_slices
=============================
Package for registering/transforming large sequences of 2D images. It contains two plugins: [Register Virtual Stack Slices](http://fiji.sc/Register_Virtual_Stack_Slices) and [Transform Virtual Stack Slices](http://fiji.sc/Transform_Virtual_Stack_Slices).

[Register Virtual Stack Slices](http://fiji.sc/Register_Virtual_Stack_Slices) takes a sequence of image slices stored in a folder, and delivers a list of registered image slices (with enlarged canvas). One of the images in the sequence can be selected by the user as reference and it will remain intact.

The plugin can perform 6 types of image registration techniques:

- Translation
- Rigid (translation + rotation)
- Similarity (translation + rotation + isotropic scaling)
- Affine
- Elastic (via bUnwarpJ with cubic B-splines)
- Moving least squares

All models are aided by automatically extracted SIFT features.

For further details, visit the documentation site: http://fiji.sc/Register_Virtual_Stack_Slices

[Transform Virtual Stack Slices](http://fiji.sc/Transform_Virtual_Stack_Slices) allows you to apply the transforms calculated by [Register Virtual Stack Slices](http://fiji.sc/Register_Virtual_Stack_Slices) to the same or another set of images.
