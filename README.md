Caveat emptor: this is __experimental code__,
which will be later merged elsewhere!

Brought to you by:

* Stephan "Convoluted" Saalfeld
* Curtis "Typewriter" Rueden
* Tobias "Meta" Pietzsch
* Christian "Space Tree" Dietz

**Motivation**

Metadata is incredibly important in scientific image processing. The ImgLib2 ecosystem has no formal mechanism for working with metadata. This repository aims to solve that.

The main metadata stand-in throughout the Fiji ecosystem is the `Dataset` class of ImageJ2, used by e.g. SCIFIO. However, it has a number of issues:
* It cannot operate within a type-safe environment. This hinders use within ImgLib2-algorithm, SciJava Ops, etc.
* It requires the *massive* dependency of imagej-common.

**Current Status**

The current design is centered around a few interfaces:
* The `MetadataItem` is a piece of metadata in an n-dimensional data space.
  * It can be identified by a `String key` and a set of "attached" dimensional axes.
    * Metadata *can* (but may not) vary along each of its attached axes, e.g.:
      * Channel LUTs might be a 1-dimensional piece of metadata "attached" to the channel axis. The LUT used for a pixel varies only on the channel axis.
      * An axis label (X, Y, Channel, etc.) is a 0-dimensional piece of metadata "attached" to some axis. It does not vary along that axis.
      * ROIs are n-dimensional pieces of metadata "attached" to all axes.
* The `MetadataStore` is a collection of metadata, supporting retrieval (`MetadataStore.item`), agglomeration (`MetadataStore.add`) of `MetadataItem`s.
  * `MetadataStore`s can be viewed like `RandomAccessible`s can be viewed with the ImgLib2 Fluent API.
* The `Dataset` class as the fundamental currency in metadata-rich data processing.
  * A union of a `MetadataStore` and a `RandomAccessible`
  * Viewable using the fluent views API. e.g. `Dataset new = old.permute(3, 2)` should be painless.

The pain points are:
* `RealRandomAccessible` does not implement `RandomAccessible`
  * This makes it difficult to make one `Dataset` class that can wrangle both `RA` and `RRA`.
  * See [imglib/imglib2#378](https://github.com/imglib/imglib2/pull/378)
* `RandomAccessibleView` and its subclasses have difficult typing.
  * This makes it difficult to subclass `Dataset`, if we want a `DatasetInterval` or a `RealDataset`
  * The `V` type parameter of `RandomAccessibleView` is never something I'd want a user to have to type.
    * This type is present for the `RandomAccessibleView.use` method, which allows subinterfaces to get a specialized version for free (e.g `Dataset.use(Function<? super Dataset, U)`). Without that type variable this type of thing is basically impossible.
  * See [imglib/imglib2#379](https://github.com/imglib/imglib2/pull/379)