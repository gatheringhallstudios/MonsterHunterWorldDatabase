package com.gatheringhallstudios.mhworlddatabase.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.gatheringhallstudios.mhworlddatabase.data.models.ItemQuantity
import com.gatheringhallstudios.mhworlddatabase.data.models.Weapon
import com.gatheringhallstudios.mhworlddatabase.data.models.WeaponFull

import com.gatheringhallstudios.mhworlddatabase.data.models.WeaponTree
import com.gatheringhallstudios.mhworlddatabase.data.types.WeaponType
import com.gatheringhallstudios.mhworlddatabase.util.createLiveData


@Dao
abstract class WeaponDao {

//    /** Loads all of the weapons in the trees for a provided weapon type] */
//    @Query("""
//        WITH RECURSIVE upgrades_from(id, name, lang_id, weapon_type, rarity, level, path) AS
//        (SELECT w.id, wt.name, wt.lang_id, w.weapon_type, w.rarity, 0,  CAST(w.id AS TEXT)
//            FROM weapon w
//                JOIN weapon_text wt USING (id)
//            WHERE wt.lang_id = :langId
//              AND (w.weapon_type = :weaponType)
//			  AND w.previous_weapon_id IS NULL
//            UNION ALL
//            SELECT w.id, wt.name, wt.lang_id, w.weapon_type, w.rarity, level + 1, path || ">" || CAST(w.id AS TEXT)
//            FROM weapon w
//                JOIN weapon_text wt USING (id)
//                INNER JOIN upgrades_from
//                    ON w.previous_weapon_id = upgrades_from.id
//			WHERE wt.lang_id = :langId
//              AND (w.weapon_type = :weaponType)
//        )
//        SELECT *
//        FROM upgrades_from
//        ORDER BY level ASC
//          """)
//    abstract fun loadWeaponTrees(langId: String, weaponType: WeaponType): List<UpgradesFrom>

    /** Loads all of the weapons in the trees for a provided weapon type] */
    @Query("""
        SELECT w.id, w.element1, w.element1_attack, w.rarity, w.previous_weapon_id, w.weapon_type, w.attack, w.affinity, w.defense,
        w.slot_1, w.slot_2, w.slot_3, w.sharpness, w.sharpness_maxed, w.element2, w.element2_attack, w.element_hidden, w.elderseal, wt.*
        FROM weapon w
            JOIN weapon_text wt USING (id)
        WHERE wt.lang_id = :langId
            AND w.weapon_type = :weaponType
        ORDER BY w.id ASC
          """)
    abstract fun loadWeaponTrees(langId: String, weaponType: WeaponType): List<WeaponTree>

    @Query("""
        SELECT i.id item_id, it.name item_name, i.icon_name item_icon_name,
            i.category item_category, i.icon_color item_icon_color, w.quantity, w.recipe_type
         FROM weapon_recipe w
            JOIN item i
                ON w.item_id = i.id
            JOIN item_text it
                ON it.id = i.id
                AND it.lang_id = :langId
        WHERE it.lang_id = :langId
        AND w.weapon_id= :weaponId
        ORDER BY i.id
    """)
    abstract fun loadWeaponComponents(langId: String, weaponId: Int): List<ItemQuantity>

    @Query("""
        
        SELECT w.id, w.weapon_type, w.rarity, w.attack, w.attack_true, w.affinity, w.defense, w.slot_1, w.slot_2, w.slot_3, w.element1, w.element1_attack, 
        w.element2, w.element2_attack, w.element_hidden, w.sharpness, w.sharpness_maxed, w.previous_weapon_id, w.craftable, w.final AS isFinal, w.kinsect_bonus,
        w.elderseal, w.phial, w.phial_power, w.shelling, w.shelling_level, w.coating_close, w.coating_power, w.coating_poison, w.coating_paralysis, w.coating_sleep, w.coating_blast,
        w. notes, wt.name, wa.id AS ammo_id, wa.deviation, wa.special_ammo, wa.normal1_clip, wa.normal1_rapid, wa.normal1_recoil, wa.normal1_reload, wa.normal2_clip, wa.normal2_rapid,
        wa.normal2_recoil, wa.normal2_reload, wa.normal3_clip, wa.normal3_rapid, wa.normal3_recoil, wa.normal3_reload, wa.pierce1_clip, wa.pierce1_rapid, wa.pierce1_recoil, wa.pierce1_reload,
        wa.pierce2_clip, wa.pierce2_rapid, wa.pierce2_recoil, wa.pierce2_reload, wa.pierce3_clip, wa.pierce3_rapid, wa.pierce3_recoil, wa.pierce3_reload, wa.spread1_clip, wa.spread1_rapid,
        wa.spread1_recoil, wa.spread1_reload, wa.spread2_clip, wa.spread2_rapid, wa.spread2_recoil, wa.spread2_reload, wa.spread3_clip, wa.spread3_rapid, wa.spread3_recoil, wa.spread3_reload,
        wa.sticky1_clip, wa.sticky1_rapid, wa.sticky1_recoil, wa.sticky1_reload, wa.sticky2_clip, wa.sticky2_rapid, wa.sticky2_recoil, wa.sticky2_reload, wa.sticky3_clip, wa.sticky3_rapid, wa.sticky3_recoil, wa.sticky3_reload,
        wa.cluster1_clip, wa.cluster1_rapid, wa.cluster1_recoil, wa.cluster1_reload, wa.cluster2_clip, wa.cluster2_rapid, wa.cluster2_recoil, wa.cluster2_reload, wa.cluster3_clip, wa.cluster3_rapid, wa.cluster3_recoil, wa.cluster3_reload,
        wa.recover1_clip, wa.recover1_rapid, wa.recover1_recoil, wa.recover1_reload, wa.recover2_clip, wa.recover2_rapid, wa.recover2_recoil, wa.recover2_reload, wa.poison1_clip, wa.poison1_rapid, wa.poison1_recoil, wa.poison1_reload,
        wa.poison2_clip, wa.poison2_rapid, wa.poison2_recoil, wa.poison2_reload, wa.paralysis1_clip, wa.paralysis1_rapid, wa.paralysis1_recoil, wa.paralysis1_reload, wa.paralysis2_clip, wa.paralysis2_rapid, wa.paralysis2_recoil, wa.paralysis2_reload,
        wa.sleep1_clip, wa.sleep1_rapid, wa.sleep1_recoil, wa.sleep1_reload, wa.sleep2_clip, wa.sleep2_rapid, wa.sleep2_recoil, wa.sleep2_reload, wa.exhaust1_clip, wa.exhaust1_rapid, wa.exhaust1_recoil, wa.exhaust1_reload,
        wa.exhaust2_clip, wa.exhaust2_rapid, wa.exhaust2_recoil, wa.exhaust2_reload, wa.flaming_clip, wa.flaming_rapid, wa.flaming_recoil, wa.flaming_reload, wa.water_clip, wa.water_rapid, wa.water_recoil, wa.water_reload,
        wa.freeze_clip, wa.freeze_rapid, wa.freeze_recoil, wa.freeze_reload, wa.thunder_clip, wa.thunder_rapid, wa.thunder_recoil, wa.thunder_reload, wa.dragon_clip, wa.dragon_rapid, wa.dragon_recoil, wa.dragon_reload,
        wa.slicing_clip, wa.slicing_rapid, wa.slicing_recoil, wa.slicing_reload, wa.wyvern_clip, wa.wyvern_reload, wa.demon_clip, wa.demon_recoil, wa.demon_reload, wa.armor_clip, wa.armor_recoil, wa.armor_reload, wa.tranq_clip, wa.tranq_recoil, wa.tranq_reload
        
        FROM weapon w
            JOIN weapon_text wt USING (id)
            LEFT OUTER JOIN weapon_ammo wa ON w.ammo_id = wa.id
        WHERE w.id = :weaponId
        AND wt.lang_id = :langId
        """)
    abstract fun loadWeapon(langId: String, weaponId: Int): Weapon

    fun queryRecipeComponents(langId: String, weaponId: Int): Map<String?, List<ItemQuantity>> {
        return loadWeaponComponents(langId, weaponId).groupBy { it.recipe_type }
    }

    fun loadWeaponFull(langId: String, weaponId: Int) = createLiveData {
        val weapon = loadWeapon(langId, weaponId)
        WeaponFull(
                weapon = weapon,
                recipe = queryRecipeComponents(langId, weaponId)
        )
    }
}