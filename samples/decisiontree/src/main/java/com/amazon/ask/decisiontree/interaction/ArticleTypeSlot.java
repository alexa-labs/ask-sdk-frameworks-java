package com.amazon.ask.decisiontree.interaction;

import com.amazon.ask.models.annotation.data.SlotTypeResource;
import com.amazon.ask.models.annotation.type.SlotType;
import com.amazon.ask.models.types.slot.BaseSlotValue;

@SlotType("articleType")
@SlotTypeResource("models/article_type_slot")
public class ArticleTypeSlot extends BaseSlotValue {
}
