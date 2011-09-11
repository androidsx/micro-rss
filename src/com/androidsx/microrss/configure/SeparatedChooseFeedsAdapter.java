package com.androidsx.microrss.configure;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.androidsx.microrss.R;

public class SeparatedChooseFeedsAdapter extends BaseAdapter {

	public final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
	public final ArrayAdapter<String> headers;
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedChooseFeedsAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.choose_feeds_list_header);
	}

	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}

	public Object getItem(int position) {
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return section;
			if (position < size)
				return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	/**
	 * Gets the position within the adapter counting only the real items and headers
	 * 
	 * @param itemPosition the position of the real items, not headers
	 * @return adapter position, counting sections and items
	 */
	public int getAdapterPosByItemPos(int itemPosition) {
		int countItems = 0;
		int numAdapterItems = getCount();
		int adapterPosition = 0;
		for (int adapterItems = 0; adapterItems < numAdapterItems; adapterItems++) {
			if (!isHeader(adapterItems)) {
				countItems++;
			}
			if (countItems - 1 == itemPosition) {
				adapterPosition = adapterItems;
				break;
			}
		}
		return adapterPosition;
	}

	/**
	 * Gets the position counting only the real items, not the headers
	 * 
	 * @param adapterPosition the position within the adapter
	 * @return -1 if it is a header, or item position within all the header adapters
	 */
	public int getItemPosByAdapterPos(int adapterPosition) {
		if (isHeader(adapterPosition)) {
			return -1;
		}
		
		int itemPosition = 0;
		for (int adapterItems = 0; adapterItems < adapterPosition; adapterItems++) {
			if (!isHeader(adapterItems)) {
				itemPosition++;
			}
		}
		
		return itemPosition;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isHeader(int position) {
		return (getItemViewType(position) == TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return headers.getView(sectionnum, convertView, parent);
			if (position < size)
				return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
