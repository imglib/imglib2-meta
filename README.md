Caveat emptor: this is __experimental code__,
which will be later merged elsewhere!

Brought to you by:

* Stephan "Convoluted" Saalfeld
* Curtis "Typewriter" Rueden
* Tobias "Meta" Pietzsch
* Christian "Space Tree" Dietz

**Current Status**

The current design goals are:
* The `Dataset` class as the fundamental currency in metadata-rich data processing.
  * A union of a `MetadataStore` and a `RandomAccessible`
  * Viewable using the fluent views API. e.g. `Dataset new = old.permute(3, 2)` should be painless.
  * Painless typing. Ideally just the one type variable for the 

The pain points are:
* `RealRandomAccessible` does not implement `RandomAccessible`
  * This makes it difficult to make one `Dataset` class that can wrangle both `RA` and `RRA`.
* `RandomAccessibleView` and its subclasses have difficult typing.
  * This makes it difficult to subclass `Dataset`, if we want a `DatasetInterval` or a `RealDataset`
  * The `V` type parameter of `RandomAccessibleView` is never something I'd want a user to have to type.
    * This type is present for the `RandomAccessibleView.use` method, which allows subinterfaces to get a specialized version for free (e.g `Dataset.use(Function<? super Dataset, U)`). Without that type variable this type of thing is basically impossible.