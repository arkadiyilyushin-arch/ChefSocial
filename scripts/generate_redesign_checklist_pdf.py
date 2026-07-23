#!/usr/bin/env python3
"""Generate Chefly redesign checklist PDF."""

from fpdf import FPDF

FONT = "/System/Library/Fonts/Supplemental/Arial Unicode.ttf"
OUTPUT = "/Users/admin/Projects/CarExpenses/docs/Chefly-Redesign-Checklist.pdf"


class ChecklistPDF(FPDF):
    def footer(self):
        self.set_y(-15)
        self.set_font("Chefly", "", 9)
        self.set_text_color(120, 120, 120)
        self.cell(0, 10, f"Страница {self.page_no()}", align="C")

    def section_title(self, title: str):
        self.ln(5)
        self.set_font("Chefly", "B", 14)
        self.set_text_color(209, 93, 65)
        self.multi_cell(self.epw, 8, title)
        self.ln(1)

    def item(self, text: str, done: bool = False):
        self.set_x(self.l_margin)
        self.set_font("Chefly", "", 11)
        self.set_text_color(40, 40, 40)
        mark = "[x]" if done else "[ ]"
        self.multi_cell(self.epw, 6, f"{mark} {text}")

    def note(self, text: str):
        self.set_x(self.l_margin)
        self.set_font("Chefly", "", 10)
        self.set_text_color(100, 100, 100)
        self.multi_cell(self.epw, 5, text)
        self.ln(2)

    def table_row(self, cols: list[str], bold: bool = False):
        self.set_x(self.l_margin)
        self.set_font("Chefly", "B" if bold else "", 10)
        self.set_text_color(40, 40, 40)
        widths = [18, 52, 110]
        for col, w in zip(cols, widths):
            self.cell(w, 7, col, border=1)
        self.ln()


def build_pdf():
    pdf = ChecklistPDF()
    pdf.add_font("Chefly", "", FONT)
    pdf.add_font("Chefly", "B", FONT)
    pdf.set_margins(15, 15, 15)
    pdf.set_auto_page_break(auto=True, margin=18)
    pdf.add_page()

    pdf.set_font("Chefly", "B", 22)
    pdf.set_text_color(92, 61, 46)
    pdf.multi_cell(pdf.epw, 12, "Chefly")
    pdf.set_font("Chefly", "B", 16)
    pdf.multi_cell(pdf.epw, 10, "Простой чеклист изменений")
    pdf.note(
        "Все пункты, которые хотим сделать. Отмечай [ ] -> [x] по мере выполнения. "
        "Всего ~55 пунктов (5 готово, 5 опциональных)."
    )

    pdf.section_title("1. Основа (сделать первым)")
    for t in [
        "Единые отступы и скругления на всех экранах",
        "Единая типографика: заголовки крупнее, подписи контрастнее",
        "Все цвета только через тему (без белого фона + светлый текст)",
        "Тёмная тема работает везде, не только на части экранов",
        "Проверить контраст текста (чтобы всегда было видно)",
    ]:
        pdf.item(t)

    pdf.section_title("2. Навигация")
    for t in [
        "Разделить «Сообщения» и «Форум» (не в одной вкладке)",
        "Упростить шапку ленты (лишнее убрать в меню ...)",
        "После онбординга вести к первому действию (создать рецепт / подписаться)",
        "Одинаковые кнопки «Назад» на всех экранах",
        "Удобные переходы: профиль -> рецепт -> автор -> сообщение",
    ]:
        pdf.item(t)

    pdf.section_title("3. Вход и первый запуск")
    pdf.item("Поля логина/регистрации хорошо читаются (v1.6.7 — проверить)")
    for t in [
        "Понятные подписи к полям и подсказка для пароля",
        "Нормальная иконка «показать пароль»",
        "Онбординг с картинками вместо emoji, точки страниц, понятно что листать",
        "Splash-экран с логотипом Chefly при запуске",
        "«Забыли пароль?» — нормальный экран, не заглушка",
    ]:
        pdf.item(t)

    pdf.section_title("4. Лента")
    for t in [
        "Крупные фото рецептов (food-first)",
        "Фильтры категорий закреплены при скролле",
        "Pull-to-refresh (потянул — обновил)",
        "Красивый пустой экран: «Подпишись» + «Создай рецепт»",
        "Загрузка skeleton вместо крутилки",
        "(опционально) Stories-кружки сверху",
        "(опционально) Двойной тап = лайк",
    ]:
        pdf.item(t)

    pdf.add_page()

    pdf.section_title("5. Рецепт (просмотр и создание)")
    for t in [
        "Большое фото сверху, название поверх картинки",
        "Кнопки внизу: лайк · коммент · сохранить · поделиться",
        "Ингредиенты и шаги — numbered list, удобно готовить",
        "Создание рецепта по шагам (не одна длинная форма)",
        "Превью перед публикацией",
        "Явная кнопка «Сканировать рецепт» (камера/OCR)",
    ]:
        pdf.item(t)

    pdf.section_title("6. Профиль")
    pdf.item("Instagram-layout, сетка, вкладки (v1.6.6 — готово)", done=True)
    pdf.item("Share, сообщение, закреп, «Понравилось» (v1.6.6 — готово)", done=True)
    pdf.item("Контраст текста (v1.6.7 — проверить)")
    for t in [
        "Кнопки «Редактировать / Настройки» — красивее, не «служебные»",
        "Редактирование профиля с live-preview",
        "Приватность профиля реально работает (не только в настройках)",
        "(опционально) Highlights-кружки как в Instagram",
    ]:
        pdf.item(t)

    pdf.section_title("7. Новости")
    pdf.item("Текст в карточках читается (v1.6.7 — проверить)")
    for t in [
        "Фильтры в том же стиле, что и в ленте (chips)",
        "Закреплённая новость — крупная карточка сверху",
        "Экран новости — удобно читать, кнопка «Поделиться»",
        "Превью при создании новости (админ)",
    ]:
        pdf.item(t)

    pdf.section_title("8. Сообщения и форум")
    for t in [
        "Отдельный UI для чатов и форума",
        "Современные chat bubbles",
        "Аватары в списке диалогов",
        "Форум — карточки с числом ответов",
        "Пустой экран с кнопкой «Найти повара в ленте»",
    ]:
        pdf.item(t)

    pdf.section_title("9. Поиск и рейтинг")
    for t in [
        "Поиск: вкладки «Рецепты» / «Повара»",
        "Недавние поиски",
        "Лидерборд: топ-3 на пьедестале, медали",
        "(опционально) Блок «Популярное» / «Новые повара» на ленте",
    ]:
        pdf.item(t)

    pdf.add_page()

    pdf.section_title("10. Настройки")
    pdf.item("Версия в шапке (v1.6.5 — готово)", done=True)
    for t in [
        "Меньше визуального шума (редкие разделы свернуть)",
        "Выбор темы с preview (светлая / тёмная)",
        "Убрать дубли: настройки только там, где логично",
        "Убрать оранжевый banner «новая версия» когда всё стабильно",
    ]:
        pdf.item(t)

    pdf.section_title("11. Общий вид приложения")
    for t in [
        "Логотип Chefly вместо emoji на входе",
        "Иллюстрации для пустых экранов",
        "Плавная загрузка фото (placeholder -> fade-in)",
        "Убрать декоративные emoji из фона",
        "(опционально) Свои иконки категорий блюд",
        "(опционально) Haptic при лайке и подписке",
    ]:
        pdf.item(t)

    pdf.section_title("12. Техническое (чтобы не ломалось снова)")
    for t in [
        "Нормальные номера релизов в CI (v1.6.x, не v1.2.x)",
        "Preview/тесты экранов (чтобы тема не ломалась)",
        "Accessibility: описания для иконок, контраст",
    ]:
        pdf.item(t)

    pdf.section_title("Уже сделано")
    pdf.item("Профиль Instagram-style (v1.6.6)", done=True)
    pdf.item("Настройки, версия, кнопка «Настройки» (v1.6.5)", done=True)
    pdf.item("Контраст светлой темы (v1.6.7)", done=True)

    pdf.ln(4)
    pdf.section_title("С чего логично начать")
    pdf.table_row(["Шаг", "Что", "Зачем"], bold=True)
    pdf.table_row(["1", "Раздел 1 (Основа)", "Чтобы нигде не пропадал текст"])
    pdf.table_row(["2", "Разделы 4 + 5", "Лента + рецепт — главное"])
    pdf.table_row(["3", "Раздел 2 + 8", "Навигация + общение"])
    pdf.table_row(["4", "Раздел 11", "Красиво для показа людям"])

    pdf.output(OUTPUT)
    print(OUTPUT)


if __name__ == "__main__":
    build_pdf()
