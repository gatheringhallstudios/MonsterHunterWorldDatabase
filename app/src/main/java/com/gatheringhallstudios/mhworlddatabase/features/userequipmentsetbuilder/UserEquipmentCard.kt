package com.gatheringhallstudios.mhworlddatabase.features.userequipmentsetbuilder

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.gatheringhallstudios.mhworlddatabase.R
import com.gatheringhallstudios.mhworlddatabase.assets.AssetLoader
import com.gatheringhallstudios.mhworlddatabase.assets.SetBonusNumberRegistry
import com.gatheringhallstudios.mhworlddatabase.assets.SlotEmptyRegistry
import com.gatheringhallstudios.mhworlddatabase.components.ExpandableCardView
import com.gatheringhallstudios.mhworlddatabase.data.models.*
import com.gatheringhallstudios.mhworlddatabase.data.types.ArmorType
import com.gatheringhallstudios.mhworlddatabase.features.userequipmentsetbuilder.selectors.UserEquipmentSetSelectorListFragment
import com.gatheringhallstudios.mhworlddatabase.getRouter
import com.gatheringhallstudios.mhworlddatabase.util.getDrawableCompat
import kotlinx.android.synthetic.main.cell_expandable_cardview.view.*
import kotlinx.android.synthetic.main.listitem_armorset_bonus.view.*
import kotlinx.android.synthetic.main.listitem_skill_description.view.level_text
import kotlinx.android.synthetic.main.listitem_skill_level.view.*
import kotlinx.android.synthetic.main.view_base_body_expandable_cardview.view.*
import kotlinx.android.synthetic.main.view_base_header_expandable_cardview.view.*
import kotlinx.android.synthetic.main.view_base_header_expandable_cardview.view.icon_slots
import kotlinx.android.synthetic.main.view_base_header_expandable_cardview.view.slot1
import kotlinx.android.synthetic.main.view_base_header_expandable_cardview.view.slot2
import kotlinx.android.synthetic.main.view_base_header_expandable_cardview.view.slot3
import kotlinx.android.synthetic.main.view_base_header_expandable_cardview.view.slot_section as BaseSlotSection
import kotlinx.android.synthetic.main.view_empty_equipment_header_expandable_cardview.view.*
import kotlinx.android.synthetic.main.view_weapon_header_expandable_cardview.view.*
import kotlinx.android.synthetic.main.view_weapon_header_expandable_cardview.view.equipment_icon
import kotlinx.android.synthetic.main.view_weapon_header_expandable_cardview.view.equipment_name
import kotlinx.android.synthetic.main.view_weapon_header_expandable_cardview.view.rarity_string

/**
 * Wrapper over the ExpandableCardView used to display equipment data.
 * Used to
 */
class UserEquipmentCard(private val card: ExpandableCardView) {
    private fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return card.resources.getString(resId, *formatArgs)
    }

    fun bindActiveWeapon(userWeapon: UserWeapon?) {
        if (userWeapon != null) {
            bindWeapon(userWeapon.weapon, null, null)
            card.setCardElevation(2f)
        } else {
            bindEmptyWeapon()
        }
    }

    /**
    Binds a clickable user weapon card to the encapsulated card
     */
    fun bindWeapon(userWeapon: UserWeapon?, setId: Int, onClick: (() -> Unit)?, onSwipeRight: (() -> Unit)?) {
        if (userWeapon != null) {
            bindWeapon(userWeapon.weapon, onClick, onSwipeRight)

            //Repopulate the skills section to include the decoration skills
            card.card_body.skill_list.removeAllViews()
            val skillsList = combineEquipmentSkillsWithDecorationSkills(userWeapon.weapon.skills, userWeapon.decorations.map {
                val skillLevel = SkillLevel(level = 1)
                skillLevel.skillTree = it.decoration.skillTree
                skillLevel
            })

            populateSkills(skillsList)
        } else {
            bindEmptyWeapon()
            card.setOnClick {
                card.getRouter().navigateUserEquipmentPieceSelector(UserEquipmentSetSelectorListFragment.Companion.SelectorMode.WEAPON,
                        null, setId, null, null)
            }
        }
    }

    /**
     * Binds a weapon entity to the encapsulated card
     */
    fun bindWeapon(weaponFull: WeaponFull, onClick: (() -> Unit)?, onSwipeRight: (() -> Unit)?) {
        val weapon = weaponFull.weapon
        with(card) {
            setHeader(R.layout.view_weapon_header_expandable_cardview)
            setBody(R.layout.view_base_body_expandable_cardview)
            setCardElevation(1f)
        }

        with (card.card_header) {
            equipment_name.text = weapon.name
            equipment_icon.setImageDrawable(AssetLoader.loadIconFor(weapon))
            attack_value.text = weapon.attack.toString()
        }

        with (card.card_body) {
            decorations_section.visibility = View.GONE
        }

        bindRarity(weapon.rarity)
        populateSkills(weaponFull.skills)
        populateSetBonuses(emptyList())

        if (onClick != null) card.setOnClick(onClick)
        if (onSwipeRight != null) card.setOnSwipeRight(onSwipeRight)
    }

    /**
     *
     */
    fun bindActiveArmor(userArmor: UserArmorPiece?, armorType: ArmorType) {
        if (userArmor != null) {
            bindArmor(userArmor.armor, null, null)
            card.setCardElevation(2f)
        } else {
            bindEmptyArmor(armorType)
        }
    }

    /**
    Binds a clickable head armor card to the encapsulated card
     */
    fun bindHeadArmor(userArmor: UserArmorPiece?, setId: Int, onClick: () -> Unit, onSwipeRight: () -> Unit) {
        bindUserArmor(userArmor, ArmorType.HEAD, setId, onClick, onSwipeRight)
    }

    /**
    Binds a clickable arm armor card to the encapsulated card
     */
    fun bindArmArmor(userArmor: UserArmorPiece?, setId: Int, onClick: () -> Unit, onSwipeRight: () -> Unit) {
        bindUserArmor(userArmor, ArmorType.ARMS, setId, onClick, onSwipeRight)
    }

    /**
    Binds a clickable chest armor card to the encapsulated card
     */
    fun bindChestArmor(userArmor: UserArmorPiece?, setId: Int, onClick: () -> Unit, onSwipeRight: () -> Unit) {
        bindUserArmor(userArmor, ArmorType.CHEST, setId, onClick, onSwipeRight)
    }

    /**
    Binds a clickable leg armor card to the encapsulated card
     */
    fun bindLegArmor(userArmor: UserArmorPiece?, setId: Int, onClick: () -> Unit, onSwipeRight: () -> Unit) {
        bindUserArmor(userArmor, ArmorType.LEGS, setId, onClick, onSwipeRight)
    }

    /**
    Binds a view only waist armor card to the encapsulated card
     */
    fun bindWaistArmor(userArmor: UserArmorPiece?, setId: Int, onClick: () -> Unit, onSwipeRight: () -> Unit) {
        bindUserArmor(userArmor, ArmorType.WAIST, setId, onClick, onSwipeRight)
    }

    private fun bindUserArmor(userArmor: UserArmorPiece?, armorType: ArmorType, setId: Int? = null,
                              onClick: (() -> Unit)? = null, onSwipeRight: (() -> Unit)? = null) {
        if (userArmor != null) {
            val armor = userArmor.armor
            bindArmor(armor, onClick, onSwipeRight)
            //Repopulate the skills section to include the decoration skills
            card.card_body.skill_list.removeAllViews()
            val skillsList = combineEquipmentSkillsWithDecorationSkills(armor.skills, userArmor.decorations.map {
                val skillLevel = SkillLevel(level = 1)
                skillLevel.skillTree = it.decoration.skillTree
                skillLevel
            })

            populateSkills(skillsList)
        } else {
            bindEmptyArmor(armorType)
            card.setOnClick {
                card.getRouter().navigateUserEquipmentPieceSelector(UserEquipmentSetSelectorListFragment.Companion.SelectorMode.ARMOR,
                        null, setId, armorType, null)
            }
        }
    }

    fun bindArmor(armor: ArmorFull, onClick: (() -> Unit)?, onSwipeRight: (() -> Unit)?) {
        with(card) {
            setHeader(R.layout.view_base_header_expandable_cardview)
            setBody(R.layout.view_base_body_expandable_cardview)
            setCardElevation(1f)
        }

        with (card.card_header) {
            equipment_name.text = armor.armor.name
            equipment_icon.setImageDrawable(AssetLoader.loadIconFor(armor.armor))
            defense_value.text = getString(
                    R.string.armor_defense_value,
                    armor.armor.defense_base,
                    armor.armor.defense_max,
                    armor.armor.defense_augment_max)
        }

        with (card.card_body) {
            decorations_section.visibility = View.GONE
        }

        bindRarity(armor.armor.rarity)
        populateSkills(armor.skills)
        populateSetBonuses(armor.setBonuses)
        if (onClick != null) card.setOnClick(onClick)
        if (onSwipeRight != null) card.setOnSwipeRight(onSwipeRight)
    }

    /**
     * Binds a view only active charm card to the encapsulated card. The active card is the top most card on the
     * selector fragment
     */
    fun bindActiveCharm(userCharm: UserCharm?) {
        bindCharm(userCharm)
        card.setCardElevation(2f)
    }

    /**
     * Binds a view only charm card to the encapsulated card
     */
    fun bindCharm(userCharm: UserCharm?) {
        if (userCharm != null) {
            bindCharm(userCharm.charm, null, null)
        } else {
            bindEmptyCharm()
        }
    }

    /**
     * Binds a clickable charm card to the encapsulated card
     */
    fun bindCharm(userCharm: UserCharm?, setId: Int, onClick: () -> Unit, onSwipeRight: () -> Unit) {
        if (userCharm != null) {
            bindCharm(userCharm.charm, onClick, onSwipeRight)
        } else {
            bindEmptyCharm()
            card.setOnClick {
                card.getRouter().navigateUserEquipmentPieceSelector(UserEquipmentSetSelectorListFragment.Companion.SelectorMode.CHARM,
                        null, setId, null, null)
            }
        }
    }

    /**
     * Bind a charm entity to the encapsulated card
     */
    fun bindCharm(charm: CharmFull, onClick: (() -> Unit)?, onSwipeRight: (() -> Unit)?) {
        with(card) {
            setHeader(R.layout.view_base_header_expandable_cardview)
            setBody(R.layout.view_base_body_expandable_cardview)
            setCardElevation(1f)
        }

        with (card.card_header) {
            equipment_name.text = charm.charm.name
            equipment_icon.setImageDrawable(AssetLoader.loadIconFor(charm.charm))
            defense_value.visibility = View.GONE
            icon_defense.visibility = View.GONE
        }

        with (card.card_body) {
            decorations_section.visibility = View.GONE
        }

        bindRarity(charm.charm.rarity)
        populateSkills(charm.skills)
        populateSetBonuses(emptyList())
        hideSlots()

        if (onClick != null) card.setOnClick(onClick)
        if (onSwipeRight != null) card.setOnSwipeRight(onSwipeRight)
    }

    /**
     * Internal function to enable the rarity string and display the value
     */
    private fun bindRarity(rarity: Int) {
        with (card.card_header) {
            rarity_string.text = getString(R.string.format_rarity, rarity)
            rarity_string.setTextColor(AssetLoader.loadRarityColor(rarity))
            rarity_string.visibility = View.VISIBLE
        }
    }

    /**
     * Binds a view only decoration card to the encapsulated card
     */
    fun bindDecoration(userDecoration: UserDecoration?, slotSize: Int) {
        if (userDecoration != null) {
            bindDecoration(userDecoration.decoration, null)
        } else {
            bindEmptyDecoration(slotSize)
        }
    }

    /**
     * Bind a charm entity to the encapsulated card
     */
    fun bindDecoration(decoration: Decoration, onClick: (() -> Unit)?) {
        with(card) {
            setHeader(R.layout.view_base_header_expandable_cardview)
            setBody(R.layout.view_base_body_expandable_cardview)
            setCardElevation(1f)

            setOnClick {
                onClick?.invoke()
            }
        }

        with (card.card_header) {
            equipment_name.text = decoration.name
            equipment_icon.setImageDrawable(AssetLoader.loadIconFor(decoration))
            defense_value.visibility = View.GONE
            icon_defense.visibility = View.GONE
            icon_slots.visibility = View.GONE
            BaseSlotSection.visibility = View.GONE
        }

        with (card.card_body) {
            set_bonus_section.visibility = View.GONE
            decorations_section.visibility = View.GONE
        }

        bindRarity(decoration.rarity)

        val skillLevel = SkillLevel(1)
        skillLevel.skillTree = decoration.skillTree
        populateSkills(listOf(skillLevel))
    }

    fun bindEmptyWeapon() {
        setEmptyView(R.string.user_equipment_set_no_weapon, R.drawable.ic_equipment_weapon_empty)
    }

    fun bindEmptyArmor(type: ArmorType?) {
        setEmptyView(R.string.user_equipment_set_no_armor, when (type) {
            ArmorType.HEAD -> R.drawable.ic_equipment_head_empty
            ArmorType.CHEST -> R.drawable.ic_equipment_chest_empty
            ArmorType.ARMS -> R.drawable.ic_equipment_arm_empty
            ArmorType.WAIST -> R.drawable.ic_equipment_waist_empty
            ArmorType.LEGS -> R.drawable.ic_equipment_leg_empty
            else -> R.drawable.ic_equipment_armor_set_empty
        })
    }

    fun bindEmptyCharm() {
        setEmptyView(R.string.user_equipment_set_no_charm, R.drawable.ic_equipment_armor_set_empty)
    }

    fun bindEmptyDecoration(slotSize: Int) {
        setEmptyView(R.string.user_equipment_set_no_decoration, when (slotSize) {
            1 -> R.drawable.ic_ui_slot_1_empty
            2 -> R.drawable.ic_ui_slot_2_empty
            3 -> R.drawable.ic_ui_slot_3_empty
            4 -> R.drawable.ic_ui_slot_4_empty
            else -> R.drawable.ic_ui_slot_none
        })
    }

    fun populateSkills(skills: List<SkillLevel>) {
        val skill_section = card.card_body.skill_section
        val skill_list = skill_section.skill_list

        skill_list.removeAllViews()
        skill_section.isVisible = !skills.isEmpty()

        val inflater = LayoutInflater.from(card.context)
        for (skill in skills) {
            //Set the label for the Set name
            val view = inflater.inflate(R.layout.listitem_skill_level, skill_list, false)

            view.icon.setImageDrawable(AssetLoader.loadIconFor(skill.skillTree))
            view.label_text.text = skill.skillTree.name
            view.level_text.text = getString(R.string.skill_level_qty, skill.level)
            with(view.skill_level) {
                maxLevel = skill.skillTree.max_level
                level = skill.level
            }

            view.setOnClickListener {
                card.getRouter().navigateSkillDetail(skill.skillTree.id)
            }

            skill_list.addView(view)
        }
    }

    fun populateSetBonuses(setBonuses: List<ArmorSetBonus>) {
        with(card.set_bonus_section) {
            set_bonus_list.removeAllViews()
            visibility = if (setBonuses.isEmpty()) View.GONE else View.VISIBLE
        }

        val inflater = LayoutInflater.from(card.context)

        //Now to set the actual skills
        for (setBonus in setBonuses) {
            val skillIcon = AssetLoader.loadIconFor(setBonus.skillTree)
            val reqIcon = SetBonusNumberRegistry(setBonus.required)
            val listItem = inflater.inflate(R.layout.listitem_armorset_bonus, card.set_bonus_list, false)

            listItem.bonus_skill_icon.setImageDrawable(skillIcon)
            listItem.bonus_skill_name.text = setBonus.skillTree.name
            listItem.bonus_requirement.setImageResource(reqIcon)

            listItem.setOnClickListener {
                card.getRouter().navigateSkillDetail(setBonus.skillTree.id)
            }

            card.set_bonus_list.addView(listItem)
        }
    }

    /**
     * Populates the slot icons, but hides the decorations section.
     * If you want decorations to be selectable, use populateDecorations instead.
     */
    fun populateSlots(slots: EquipmentSlots?) {
        if (slots != null) {
            this.populateDecorations(slots, emptyList())
            card.decorations_section.visibility = View.GONE
        }
    }

    /**
     * Populates the decoration section for most equipment pieces.
     * For each callback, it will either return the UserDecoration that was clicked, or
     * the slot number (1-indexed) of the clicked slot.
     */
    fun populateDecorations(slots: EquipmentSlots, decorations: List<UserDecoration>,
                            onEmptyClick: ((Int) -> Unit)? = null,
                            onClick: ((Int, UserDecoration) -> Unit)? = null,
                            onDelete: ((UserDecoration) -> Unit)? = null) {
        with(card.card_body.decorations_section) {
            visibility = if (slots.isEmpty()) View.GONE else View.VISIBLE
            slot1_detail.visibility = View.GONE
            slot2_detail.visibility = View.GONE
            slot3_detail.visibility = View.GONE
        }

        // Bind decorations that exist first
        slots.active.forEachIndexed { idx, slotSize ->
            val slotNumber = idx + 1
            val userDecoration = decorations.find { it.slotNumber == slotNumber }
            val decoration = userDecoration?.decoration

            val imageView = when (slotNumber) {
                1 -> card.card_header.slot1
                2 -> card.card_header.slot2
                3 -> card.card_header.slot3
                else -> throw IndexOutOfBoundsException("SlotIdx is out of range 1-3: $slotNumber")
            }

            val detailView = when (slotNumber) {
                1 -> card.card_body.slot1_detail
                2 -> card.card_body.slot2_detail
                3 -> card.card_body.slot3_detail
                else -> throw IndexOutOfBoundsException("SlotIdx is out of range 1-3: $slotNumber")
            }

            detailView.visibility = View.VISIBLE
            detailView.removeDecorator()

            if (decoration != null) {
                imageView.setImageDrawable(AssetLoader.loadFilledSlotIcon(decoration, slotSize))
                detailView.setLabelText(decoration.name)
                detailView.setLeftIconDrawable(AssetLoader.loadFilledSlotIcon(decoration, slotSize))

                detailView.setOnClickListener {
                    onClick?.invoke(slotNumber, userDecoration)
                }

                detailView.setButtonClickFunction {
                    onDelete?.invoke(userDecoration)
                }
            } else {
                imageView.setImageDrawable(card.context!!.getDrawableCompat(SlotEmptyRegistry(slotSize)))
                detailView.setLeftIconDrawable(card.context!!.getDrawableCompat(SlotEmptyRegistry(slotSize)))
                detailView.setLabelText(getString(R.string.user_equipment_set_no_decoration))
                detailView.hideButton()

                detailView.setOnClickListener {
                    onEmptyClick?.invoke(slotNumber)
                }
            }
        }
    }


    private fun setEmptyView(@StringRes title: Int, @DrawableRes icon: Int) {
        with(card) {
            setHeader(R.layout.view_empty_equipment_header_expandable_cardview)
            setBody(R.layout.view_empty_equipment_body_expandable_cardview)
        }

        with (card.card_header) {
            new_equipment_set_label.text = getString(title)
            equipment_set_icon2.setImageResource(icon)
        }
    }

    private fun hideSlots() {
        with(card.card_header) {
            icon_slots.visibility = View.GONE
            slot1.visibility = View.GONE
            slot2.visibility = View.GONE
            slot3.visibility = View.GONE
        }
    }

    private fun combineEquipmentSkillsWithDecorationSkills(equipmentSkills: List<SkillLevel>, decorationSkills: List<SkillLevel>): List<SkillLevel> {
        val skills = equipmentSkills.associateBy({ it.skillTree.id }, { it }).toMutableMap()
        for (skill in decorationSkills) {
            if (skills.containsKey(skill.skillTree.id)) {
                val level = skills.getValue(skill.skillTree.id).level + skill.level
                val skillLevel = SkillLevel(level)
                skillLevel.skillTree = skill.skillTree
                skills[skill.skillTree.id] = skillLevel
            } else {
                skills[skill.skillTree.id] = skill
            }
        }
        val result = skills.values.toMutableList()
        result.sortWith(compareByDescending<SkillLevel> { it.level }.thenBy { it.skillTree.id })
        return result
    }
}