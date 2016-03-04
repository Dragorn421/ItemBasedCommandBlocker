package fr.dragorn421.itembasedcommandblocker;

import java.util.Collection;
import java.util.HashSet;

public class FilterList<E>
{

	final private Collection<E> list;
	final private boolean whitelist;

	public FilterList(final Collection<E> list, final boolean whitelist)
	{
		this.list = list==null?new HashSet<E>():list;
		this.whitelist = whitelist;
	}

	public boolean isAllowed(final E element)
	{
/*
						whitelist	blacklist
list contains			true		false
list doesnt contain		false		true
 */
		return this.list.contains(element) == this.whitelist;
	}

	public Collection<E> getList()
	{
		return this.list;
	}

	public boolean isWhitelist()
	{
		return this.whitelist;
	}

	@Override
	public String toString()
	{
		
		return "FilterList{whitelist=" + this.whitelist + ",list=" + this.list.toString() + "}";
	}

}
