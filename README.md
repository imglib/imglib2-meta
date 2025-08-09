Caveat emptor: this is __experimental code__,
which will be later merged elsewhere!

Brought to you by:

* Stephan "Convoluted" Saalfeld
* Curtis "Typewriter" Rueden
* Tobias "Meta" Pietzsch
* Christian "Space Tree" Dietz

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
  * Painless typing. Ideally just the one type variable for the 

The pain points are:
* `RealRandomAccessible` does not implement `RandomAccessible`
  * This makes it difficult to make one `Dataset` class that can wrangle both `RA` and `RRA`.
  * See [imglib/imglib2#378](https://github.com/imglib/imglib2/pull/378)
* `RandomAccessibleView` and its subclasses have difficult typing.
  * This makes it difficult to subclass `Dataset`, if we want a `DatasetInterval` or a `RealDataset`
  * The `V` type parameter of `RandomAccessibleView` is never something I'd want a user to have to type.
    * This type is present for the `RandomAccessibleView.use` method, which allows subinterfaces to get a specialized version for free (e.g `Dataset.use(Function<? super Dataset, U)`). Without that type variable this type of thing is basically impossible.
  * See [imglib/imglib2#379](https://github.com/imglib/imglib2/pull/379)
* It would be nice to partition out discrete data+metadata from real data+metadata. To provide typesafety and a minimal API for each set, we need separate API i.e.
  * A `MetadataStore`, containing `MetadataItem`s that can be queried in discrete space.
  * A `RealMetadataStore`, containing `RealMetadataItem`s that can be queried in real space.
  
  However, much like `RealRandomAccessible`s should be `RandomAccessible`s, there are benefits to making `RealMetadataStore` a `MetadataStore` (including using the same API for e.g. our `MetadataStore` wrapper classes like `Attribution`, `Calibration`, etc.) This is actually pretty tricky due to the typing of the `MetadataStore`, specifically those `Optional` returns. I'd like to find a simpler return type, but we'll need *some* way to find out whether metadata exists within a `MetadataStore`. Could consider adding a `MetadataStore.contains`, or a default value parameter to the `MetadataStore.item` in the case no item satisfies the request.