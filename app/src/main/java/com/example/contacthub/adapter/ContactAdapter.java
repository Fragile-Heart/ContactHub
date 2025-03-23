package com.example.contacthub.adapter;

    import android.content.Context;
    import android.content.Intent;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.contacthub.R;
    import com.example.contacthub.model.Contact;
    import com.example.contacthub.ui.contactDetail.ContactDetailActivity;

    import java.util.List;

    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
        private List<Contact> contacts;
//        private OnContactClickListener listener;
//
//        public interface OnContactClickListener {
//            void onContactClick(Contact contact);
//        }
//
//        // 设置点击监听器的方法
//        public void setOnContactClickListener(OnContactClickListener listener) {
//            this.listener = listener;
//        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Contact contact = contacts.get(position);
            holder.nameTextView.setText(contact.getName());

            // 设置点击事件
            holder.itemView.setOnClickListener(v -> {

                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, ContactDetailActivity.class);
                intent.putExtra("contact", contact);
                context.startActivity(intent);
            });
        }


        public ContactAdapter(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.tv_contact_name);
            }
        }
    }