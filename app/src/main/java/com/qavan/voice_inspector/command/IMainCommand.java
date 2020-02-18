package com.qavan.voice_inspector.command;

public interface IMainCommand extends ICommand {
    int SEARCH_SUBSCR = 0;
    int SEARCH_DEVICE = 1;

    int REFRESH_LIST = 0;

    int METER_READING_DAY = 0;

    /**
     * Обработчик поиска
     *
     * @param type тип поиска. По ПУ (SEARCH_SUBSCR), ЛС (SEARCH_DEVICE)
     * @param text текст поиска
     */
    void onSearch(int type, String text);

    /**
     * Обработчик обновлений
     *
     * @param type тип обновления. Например, список (REFRESH_LIST).
     */
    void onRefresh(int type);

    /**
     * Обработчик закрытия карточки задания
     */
    void onCloseDetail();

    /**
     * Обработчик ввода показаний
     *
     * @param type  тип шкалы. По умолчанию METER_READING_DAY
     * @param value значение показания
     */
    void onMeterReading(int type, double value);

    /**
     * Обрабочик сохранения
     */
    void onSaveDetail();
}
