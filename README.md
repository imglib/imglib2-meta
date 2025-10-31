Caveat emptor: this is __experimental code__,
which will be later merged elsewhere!

**Motivation**

Metadata is incredibly important in scientific image processing. The ImgLib2 ecosystem has no formal mechanism for working with metadata. This repository aims to solve that.

**Goals**

The library goals are:
* Compatibility with existing ImgLib2 types: data+metadata should behave like standard `RandomAccessible` / `RandomAccessibleInterval` objects.
* Transform-aware metadata: views or transforms applied to data should automatically apply to associated metadata as well.
* Type-safe, interface-driven access: a small, well-typed API for querying and composing metadata without unnecessary copying.
* Convenient structured metadata access for common formats (for example, OME).

The main metadata stand-in throughout the Fiji ecosystem is the `Dataset` class of ImageJ2, used by e.g. SCIFIO. However, it has a number of issues:
* It cannot operate within a type-safe environment. This hinders use within ImgLib2-algorithm, SciJava Ops, etc.
* It requires the dependency of imagej-common.

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
    * We could make a `DatasetView` interface that extends `RandomAccessibleView` but removes the `V` type parameter, and have `Dataset.view()` return one of those instead of returning itself.
  * See [imglib/imglib2#379](https://github.com/imglib/imglib2/pull/379)
* Creating mutable `MetadataItem`s are tedious.
  * `RandomAccessibleInterval`s are tricky to mutate - they rely on element mutability.
    * Many common-sense metadata types (e.g. String, `ColorTable`) are not mutable.
  * Do we even need metadata mutability?

**Open Questions**
* Should `MetadataItem`s have a mechanism to get back some "source" data?
  * For example, the source `String` behind a constant `MetadataItem<String>`, or the source `RandomAccessible` behind a non-constant one.
* Should `MetadataStore`s have API to add a `MetadataItem`, instead of just taking the components?

**Future Work**
Where do we go from here?
* Spec out `HasMetadataStore` interfaces for OME-NGFF metadata?
* Get those imglib PRs merged? (Note that [imglib/imglib2#379](https://github.com/imglib/imglib2/pull/379) is technically a breaking change).
* Package review?