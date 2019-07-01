package com.gatheringhallstudios.mhworlddatabase.features.userequipmentsetbuilder.detail

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gatheringhallstudios.mhworlddatabase.R
import com.gatheringhallstudios.mhworlddatabase.assets.AssetLoader
import com.gatheringhallstudios.mhworlddatabase.data.models.*
import com.gatheringhallstudios.mhworlddatabase.data.types.ArmorType
import com.gatheringhallstudios.mhworlddatabase.data.types.DataType
import com.gatheringhallstudios.mhworlddatabase.features.armor.list.compatSwitchVector
import com.gatheringhallstudios.mhworlddatabase.features.userequipmentsetbuilder.list.UserEquipmentSetListViewModel
import com.gatheringhallstudios.mhworlddatabase.getRouter
import kotlinx.android.synthetic.main.cell_expandable_cardview.view.*
import kotlinx.android.synthetic.main.fragment_user_equipment_set_editor.*

class UserEquipmentSetEditFragment : androidx.fragment.app.Fragment() {
    companion object {
        private const val defaultRowHeight = 189 //Magic height of the row with the margins included
        private const val defaultExpandAnimationDuration = 170 //Should be shorter than the 180 of the arrow
    }

    private enum class cardState {
        EXPANDING,
        COLLAPSING
    }

    /**
     * Returns the viewmodel owned by the parent fragment
     */
    private val viewModel: UserEquipmentSetDetailViewModel by lazy {
        ViewModelProviders.of(parentFragment!!).get(UserEquipmentSetDetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_equipment_set_editor, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.activeUserEquipmentSet.observe(this, Observer<UserEquipmentSet> {
            attachLayouts(it)
            populateUserEquipment(it)
        })
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isActiveUserEquipmentSetStale()) {
            val buffer = ViewModelProviders.of(activity!!).get(UserEquipmentSetListViewModel::class.java)
            viewModel.activeUserEquipmentSet.value = buffer.getEquipmentSet(viewModel.activeUserEquipmentSet.value!!.id)
        }
    }

    private fun attachLayouts(userEquipmentSet: UserEquipmentSet) {
        user_equipment_weapon_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_weapon_slot)
        }

        user_equipment_head_slot.setOnClickListener {
            getRouter().navigateUserEquipmentArmorSelector(userEquipmentSet.id, 0, ArmorType.HEAD)
        }
        user_equipment_head_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_head_slot)
        }

        user_equipment_chest_slot.setOnClickListener {
            getRouter().navigateUserEquipmentArmorSelector(userEquipmentSet.id, 0, ArmorType.CHEST)
        }
        user_equipment_chest_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_chest_slot)
        }

        user_equipment_arms_slot.setOnClickListener {
            getRouter().navigateUserEquipmentArmorSelector(userEquipmentSet.id, 0, ArmorType.ARMS)
        }
        user_equipment_arms_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_arms_slot)
        }

        user_equipment_waist_slot.setOnClickListener {
            getRouter().navigateUserEquipmentArmorSelector(userEquipmentSet.id, 0, ArmorType.WAIST)
        }
        user_equipment_waist_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_waist_slot)
        }

        user_equipment_legs_slot.setOnClickListener {
            getRouter().navigateUserEquipmentArmorSelector(userEquipmentSet.id, 0, ArmorType.LEGS)
        }
        user_equipment_legs_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_legs_slot)
        }

        user_equipment_charm_slot.setOnClickListener {
            getRouter().navigateUserEquipmentCharmSelector(userEquipmentSet.id, 0)
        }
        user_equipment_charm_slot.card_arrow.setOnClickListener {
            toggle(user_equipment_charm_slot)
        }
    }

    private fun populateUserEquipment(userEquipmentSet: UserEquipmentSet) {
        userEquipmentSet.equipment.forEach {
            when (it.type()) {
                DataType.WEAPON -> {
                    populateWeapon(it as UserWeapon)
                }
                DataType.ARMOR -> {
                    populateArmor(it as UserArmorPiece, userEquipmentSet.id)
                }
                DataType.CHARM -> {
                    populateCharm(it as UserCharm, userEquipmentSet.id)
                }
                else -> {
                } //Skip
            }
        }
    }

    private fun toggle(cardView: View) {
        val initialHeight = cardView.height
        cardView.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val targetHeight: Int = if (initialHeight == defaultRowHeight) cardView.measuredHeight else defaultRowHeight
        if (targetHeight - initialHeight > 0) {
            animateViews(initialHeight,
                    targetHeight - initialHeight,
                    cardState.EXPANDING, cardView)
            hideSlots(cardView)
        } else {
            animateViews(initialHeight,
                    initialHeight - targetHeight,
                    cardState.COLLAPSING, cardView)
            showSlots(cardView)
        }
    }

    private fun populateArmor(userArmor: UserArmorPiece, userEquipmentSetId: Int) {
        val armor = userArmor.armor
        val layout: View
        when (armor.armor.armor_type) {
            ArmorType.HEAD -> {
                layout = user_equipment_head_slot
            }
            ArmorType.CHEST -> {
                layout = user_equipment_chest_slot
            }
            ArmorType.ARMS -> {
                layout = user_equipment_arms_slot
            }
            ArmorType.WAIST -> {
                layout = user_equipment_waist_slot
            }
            ArmorType.LEGS -> {
                layout = user_equipment_legs_slot
            }
        }

        layout.equipment_name.text = armor.armor.name
        layout.rarity_string.text = getString(R.string.format_rarity, armor.armor.rarity)
        layout.rarity_string.setTextColor(AssetLoader.loadRarityColor(armor.armor.rarity))
        layout.rarity_string.visibility = View.VISIBLE
        layout.equipment_icon.setImageDrawable(AssetLoader.loadIconFor(armor.armor))
        layout.defense_value.text = getString(
                R.string.armor_defense_value,
                armor.armor.defense_base,
                armor.armor.defense_max,
                armor.armor.defense_augment_max)
        layout.setOnClickListener {
            viewModel.setActiveUserEquipment(userArmor)
            getRouter().navigateUserEquipmentArmorSelector(userEquipmentSetId, armor.entityId, armor.armor.armor_type)
        }
        for (userDecoration in userArmor.decorations) {
            when (userDecoration.slotNumber) {
                1 -> populateSlot1(layout, userDecoration.decoration)
                2 -> populateSlot2(layout, userDecoration.decoration)
                3 -> populateSlot3(layout, userDecoration.decoration)
            }
        }
    }

    private fun populateCharm(userCharm: UserCharm, userEquipmentSetId: Int) {
        user_equipment_charm_slot.equipment_name.text = userCharm.charm.charm.name
        user_equipment_charm_slot.equipment_icon.setImageDrawable(AssetLoader.loadIconFor(userCharm.charm.charm))
        user_equipment_charm_slot.rarity_string.text = getString(R.string.format_rarity, userCharm.charm.charm.rarity)
        user_equipment_charm_slot.rarity_string.setTextColor(AssetLoader.loadRarityColor(userCharm.charm.charm.rarity))
        user_equipment_charm_slot.rarity_string.visibility = View.VISIBLE
        user_equipment_charm_slot.setOnClickListener {
            viewModel.setActiveUserEquipment(userCharm)
            getRouter().navigateUserEquipmentCharmSelector(userEquipmentSetId, userCharm.charm.entityId)
        }
        hideSlots(user_equipment_charm_slot)
        hideDefense(user_equipment_charm_slot)
        user_equipment_charm_slot.card_arrow.visibility = View.INVISIBLE
    }

    private fun populateWeapon(userWeapon: UserWeapon) {
        val weapon = userWeapon.weapon.weapon
        user_equipment_weapon_slot.equipment_name.text = weapon.name
        user_equipment_weapon_slot.equipment_icon.setImageDrawable(AssetLoader.loadIconFor(weapon))
        user_equipment_weapon_slot.rarity_string.setTextColor(AssetLoader.loadRarityColor(weapon.rarity))
        user_equipment_weapon_slot.rarity_string.text = getString(R.string.format_rarity, weapon.rarity)
        user_equipment_weapon_slot.rarity_string.visibility = View.VISIBLE
        for (userDecoration in userWeapon.decorations) {
            when (userDecoration.slotNumber) {
                1 -> populateSlot1(user_equipment_weapon_slot, userDecoration.decoration)
                2 -> populateSlot2(user_equipment_weapon_slot, userDecoration.decoration)
                3 -> populateSlot3(user_equipment_weapon_slot, userDecoration.decoration)
            }
        }

        if (weapon.defense != 0) {
            val defenseValue = getString(R.string.format_plus, weapon.defense)
            user_equipment_weapon_slot.defense_value.text = defenseValue
        } else {
            user_equipment_weapon_slot.icon_defense.visibility = View.INVISIBLE
            user_equipment_weapon_slot.defense_value.visibility = View.INVISIBLE
        }
    }

    private fun animateViews(initialHeight: Int, distance: Int, animationType: cardState, cardView: View) {
        val expandAnimation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                cardView.layoutParams.height = if (animationType == cardState.EXPANDING)
                    (initialHeight + distance * interpolatedTime).toInt()
                else
                    (initialHeight - distance * interpolatedTime).toInt()

                cardView.layoutParams.height = if (animationType == cardState.EXPANDING)
                    (initialHeight + distance * interpolatedTime).toInt()
                else
                    (initialHeight - distance * interpolatedTime).toInt()
                cardView.requestLayout()

                if (animationType == cardState.EXPANDING) {
                    val drawable = cardView.card_arrow.drawable
                    if (drawable is Animatable) {
                        drawable.start()
                    }
                } else {
                    val drawable = cardView.card_arrow.drawable
                    if (drawable is Animatable) {
                        drawable.start()
                    }
                }
            }
        }

        expandAnimation.duration = defaultExpandAnimationDuration.toLong()
        cardView.startAnimation(expandAnimation)
        cardView.card_arrow.setImageResource(when (animationType) {
            cardState.EXPANDING -> compatSwitchVector(R.drawable.ic_expand_more_animated, R.drawable.ic_expand_more)
            cardState.COLLAPSING -> compatSwitchVector(R.drawable.ic_expand_less_animated, R.drawable.ic_expand_less)
        })
    }

    private fun populateSlot1(view: View, decoration: Decoration) {
        view.slot1.setImageDrawable(AssetLoader.loadIconFor(decoration))
        view.slot1_detail.setLabelText(decoration.name)
        view.slot1_detail.setLeftIconDrawable(AssetLoader.loadIconFor(decoration))
    }

    private fun populateSlot2(view: View, decoration: Decoration) {
        view.slot2.setImageDrawable(AssetLoader.loadIconFor(decoration))
        view.slot2_detail.setLabelText(decoration.name)
        view.slot2_detail.setLeftIconDrawable(AssetLoader.loadIconFor(decoration))
    }

    private fun populateSlot3(view: View, decoration: Decoration) {
        view.slot3.setImageDrawable(AssetLoader.loadIconFor(decoration))
        view.slot3_detail.setLabelText(decoration.name)
        view.slot3_detail.setLeftIconDrawable(AssetLoader.loadIconFor(decoration))
    }

    private fun hideSlots(view: View) {
        view.icon_slots.visibility = View.INVISIBLE
        view.slot1.visibility = View.INVISIBLE
        view.slot2.visibility = View.INVISIBLE
        view.slot3.visibility = View.INVISIBLE
    }

    private fun showSlots(view: View) {
        view.icon_slots.visibility = View.VISIBLE
        view.slot1.visibility = View.VISIBLE
        view.slot2.visibility = View.VISIBLE
        view.slot3.visibility = View.VISIBLE
    }

    private fun hideDefense(view: View) {
        view.icon_defense.visibility = View.INVISIBLE
        view.defense_value.visibility = View.INVISIBLE
    }
}