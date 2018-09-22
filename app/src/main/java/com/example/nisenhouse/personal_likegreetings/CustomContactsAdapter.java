package com.example.nisenhouse.personal_likegreetings;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.nisenhouse.personal_likegreetings.databinding.RowContactViewBinding;

import java.util.ArrayList;
import java.util.List;

public class CustomContactsAdapter extends BaseAdapter implements Filterable {
    private List<WhatsAppContact> filteredData;
    private List<WhatsAppContact> listWhatsAppContact;
    private ItemTextualFilter mTextualFilter = new ItemTextualFilter();
    private Activity activity;

    public CustomContactsAdapter(Activity activity, List<WhatsAppContact> listWhatsAppContact) {
        this.listWhatsAppContact = listWhatsAppContact;
        this.filteredData = listWhatsAppContact;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RowContactViewBinding binding;
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.row_contact_view, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (RowContactViewBinding) convertView.getTag();
        }
        binding.setContact(filteredData.get(position));
        return binding.getRoot();
    }

    public Filter getFilter() {
        return mTextualFilter;
    }

    public class ItemTextualFilter extends Filter {
        private boolean filterChecked = false;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            final List<WhatsAppContact> list = listWhatsAppContact;

            int count = list.size();
            final ArrayList<WhatsAppContact> nlist = new ArrayList<>(count);

            for (WhatsAppContact wc : listWhatsAppContact) {
                boolean contained = wc.getName().toLowerCase().contains(constraint) ||
                        wc.getFullName().toLowerCase().contains(constraint);
                if (contained && (!this.filterChecked || wc.isChecked())) {
                    nlist.add(wc);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<WhatsAppContact>) results.values;
            notifyDataSetChanged();
        }

        public void setFilterChecked(boolean filter) {
            this.filterChecked = filter;
        }

    }
}
