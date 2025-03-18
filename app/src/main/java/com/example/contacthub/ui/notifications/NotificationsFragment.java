package com.example.contacthub.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.contacthub.databinding.FragmentNotificationsBinding;
import com.example.contacthub.model.Contact;
import com.example.contacthub.widget.ContactCardView;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                       ViewGroup container, Bundle savedInstanceState) {
    // Remove ViewModel initialization
    binding = FragmentNotificationsBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    // Setup the contact card with sample data
    setupContactCard();

    return root;
    }

    private void setupContactCard() {
    // Create sample contact data
    Contact sampleContact = new Contact();
    sampleContact.setName("张三");
    sampleContact.setMobileNumber("138-8888-8888");
    sampleContact.setTelephoneNumber("010-1234-5678");
    sampleContact.setEmail("zhangsan@example.com");
    sampleContact.setAddress("北京市海淀区中关村大街1号");

    // Set the contact to the card view
    ContactCardView contactCard = binding.contactCard;
    contactCard.setContact(sampleContact);
    }

    @Override
    public void onDestroyView() {
    super.onDestroyView();
    binding = null;
    }
}