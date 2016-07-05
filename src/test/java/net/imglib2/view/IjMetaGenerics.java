package net.imglib2.view;

public class IjMetaGenerics
{
	public interface ViewFactory< S >
	{
		S createView( S source );
	}

	public interface Space< S extends Space< S, T >, T >
	{
		T type();

		Space< S, T > access();

		ViewFactory< S > viewFactory();
	}

	static class SpaceView< S extends Space< S, T >, T > implements Space< S, T >
	{
		private final ViewFactory< S > viewFactory;

		private final S source;

		public SpaceView( final ViewFactory< S > viewFactory, final S source )
		{
			this.viewFactory = viewFactory;
			this.source = source;
		}

		@Override
		public T type()
		{
			return source.type();
		}

		@Override
		public Space< S, T > access()
		{
			return new Access();
		}

		class Access implements Space< S, T >
		{
			@Override
			public T type()
			{
				return SpaceView.this.type();
			}

			@Override
			public Space< S, T > access()
			{
				return SpaceView.this.access();
			}

			@Override
			public ViewFactory< S > viewFactory()
			{
				return SpaceView.this.viewFactory();
			}
		}

		@Override
		public ViewFactory< S > viewFactory()
		{
			return viewFactory;
		}
	}

	static abstract class SpaceContainer< S extends Space< S, T >, T > implements Space< S, T >
	{
		private final T type;

		private final ViewFactory< S > viewFactory;

		public SpaceContainer( final ViewFactory< S > viewFactory, final T type )
		{
			this.viewFactory = viewFactory;
			this.type = type;
		}

		@Override
		public T type()
		{
			return type;
		}

		@Override
		public Space< S, T > access()
		{
			return this;
		}

		@Override
		public ViewFactory< S > viewFactory()
		{
			return viewFactory;
		}
	}

	static interface IjSpace extends Space< IjSpace, String >
	{}

	static class IjSpaceViewFactory implements ViewFactory< IjSpace >
	{
		static IjSpaceViewFactory instance = new IjSpaceViewFactory();

		private IjSpaceViewFactory()
		{}

		@Override
		public IjSpace createView( final IjSpace source )
		{
			return new IjSpaceView( source );
		}
	}

	static class IjSpaceView extends SpaceView< IjSpace, String > implements IjSpace
	{
		public IjSpaceView( final IjSpace source )
		{
			super( IjSpaceViewFactory.instance, source );
		}
	}

	static class IjSpaceContainer extends SpaceContainer< IjSpace, String > implements IjSpace
	{
		public IjSpaceContainer( final String type )
		{
			super( IjSpaceViewFactory.instance, type );
		}
	}

	public static < S extends Space< S, T >, T > S view( final S source )
	{
		return source.viewFactory().createView( source );
	}

	public static void main( final String[] args )
	{
		final IjSpace c = new IjSpaceContainer( "blah " );
		final IjSpace v = view( c );
		final IjSpace vv = view( v );

		System.out.print( c.type() );
		System.out.print( v.type() );
		System.out.print( vv.type() );
		System.out.println();

		System.out.print( c.access().type() );
		System.out.print( v.access().type() );
		System.out.print( vv.access().type() );
		System.out.println();
	}
}
