package com.chefsocial.data

object DatabaseSeeder {
    suspend fun seed(db: AppDatabase) {
        val chefDao = db.chefDao()
        val recipeDao = db.recipeDao()
        val followDao = db.followDao()
        val likeDao = db.likeDao()

        val me = chefDao.insert(
            ChefEntity(
                name = "Вы",
                username = "you",
                bio = "Домашний повар. Люблю экспериментировать с рецептами.",
                specialty = "Домашняя кухня",
                avatarEmoji = "👨‍🍳",
                isCurrentUser = true,
            ),
        )

        val anna = chefDao.insert(
            ChefEntity(
                name = "Анна Петрова",
                username = "anna_baker",
                bio = "Кондитер с 10-летним стажем. Делюсь десертами и выпечкой.",
                specialty = "Выпечка и десерты",
                avatarEmoji = "🧁",
            ),
        )

        val ivan = chefDao.insert(
            ChefEntity(
                name = "Иван Смирнов",
                username = "chef_ivan",
                bio = "Шеф-повар итальянской кухни. Паста — моя страсть.",
                specialty = "Итальянская кухня",
                avatarEmoji = "🍝",
            ),
        )

        val maria = chefDao.insert(
            ChefEntity(
                name = "Мария Козлова",
                username = "healthy_maria",
                bio = "Нутрициолог и автор здоровых рецептов без лишних калорий.",
                specialty = "Здоровое питание",
                avatarEmoji = "🥗",
            ),
        )

        val dmitry = chefDao.insert(
            ChefEntity(
                name = "Дмитрий Волков",
                username = "grill_master",
                bio = "Мастер гриля и барбекю. Мясо, огонь и дым.",
                specialty = "Гриль и BBQ",
                avatarEmoji = "🔥",
            ),
        )

        followDao.insert(FollowEntity(followerId = me, followingId = anna))
        followDao.insert(FollowEntity(followerId = me, followingId = ivan))
        followDao.insert(FollowEntity(followerId = me, followingId = maria))
        followDao.insert(FollowEntity(followerId = anna, followingId = ivan))
        followDao.insert(FollowEntity(followerId = ivan, followingId = anna))

        val recipes = listOf(
            RecipeEntity(
                authorId = anna,
                title = "Медовик классический",
                description = "Нежный медовый торт с кремом на сметане — проверенный семейный рецепт.",
                ingredients = "Мёд — 3 ст.л.\nСахар — 200 г\nЯйца — 3 шт.\nМука — 350 г\nСода — 1 ч.л.\nСметана 20% — 800 г\nСахарная пудра — 150 г",
                steps = "1. Растопите мёд с сахаром и сливочным маслом.\n2. Добавьте яйца и муку, замесите тесто.\n3. Испеките 6-8 коржей по 7 минут.\n4. Взбейте сметану с пудрой для крема.\n5. Промажьте коржи кремом и дайте настояться 6 часов.",
                cookTimeMinutes = 90,
                servings = 10,
                difficulty = "Средне",
                category = "baking",
                imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800",
            ),
            RecipeEntity(
                authorId = ivan,
                title = "Карбонара по-римски",
                description = "Настоящая паста карбонара без сливок — только яйцо, сыр и guanciale.",
                ingredients = "Спагетти — 400 г\nGuanciale — 200 г\nЯичные желтки — 4 шт.\nПекорино — 100 г\nЧёрный перец — по вкусу",
                steps = "1. Отварите пасту al dente.\n2. Обжарьте guanciale до хруста.\n3. Смешайте желтки с тертым сыром.\n4. Соедините пасту с guanciale, снимите с огня.\n5. Добавьте яичную смесь, быстро перемешайте.",
                cookTimeMinutes = 25,
                servings = 4,
                difficulty = "Легко",
                category = "italian",
                imageUrl = "https://images.unsplash.com/photo-1621996346565-e3dbc646d45a?w=800",
            ),
            RecipeEntity(
                authorId = maria,
                title = "Боул с киноа и авокадо",
                description = "Сбалансированный обед: белок, полезные жиры и свежие овощи.",
                ingredients = "Киноа — 150 г\nАвокадо — 1 шт.\nОгурец — 1 шт.\nПомидоры черри — 150 г\nНут — 100 г\nЛимонный сок — 2 ст.л.\nОливковое масло — 1 ст.л.",
                steps = "1. Отварите киноа.\n2. Нарежьте овощи и авокадо.\n3. Смешайте все ингредиенты в миске.\n4. Заправьте лимоном и маслом.\n5. Подавайте сразу.",
                cookTimeMinutes = 20,
                servings = 2,
                difficulty = "Легко",
                category = "healthy",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
            ),
            RecipeEntity(
                authorId = dmitry,
                title = "Стейк рибай на гриле",
                description = "Сочный стейк medium rare с ароматной корочкой и розмарином.",
                ingredients = "Рибай — 400 г\nСоль — по вкусу\nЧёрный перец — по вкусу\nРозмарин — 2 веточки\nЧеснок — 3 зубчика\nСливочное масло — 30 г",
                steps = "1. Достаньте мясо за 30 мин до готовки.\n2. Разогрейте гриль до 250°C.\n3. Посолите и поперчите стейк.\n4. Жарьте 4 мин с каждой стороны.\n5. Добавьте масло, чеснок и розмарин, дайте отдохнуть 5 мин.",
                cookTimeMinutes = 35,
                servings = 2,
                difficulty = "Средне",
                category = "grill",
                imageUrl = "https://images.unsplash.com/photo-1546833990-b9f581a1996d?w=800",
            ),
            RecipeEntity(
                authorId = anna,
                title = "Круассаны домашние",
                description = "Хрустящие слоёные круассаны — терпение окупается результатом.",
                ingredients = "Мука — 500 г\nМолоко — 250 мл\nДрожжи — 7 г\nСливочное масло — 250 г\nСахар — 50 г\nСоль — 10 г\nЯйцо — 1 шт.",
                steps = "1. Замесите тесто, дайте подойти.\n2. Раскатайте и заверните масло слоями.\n3. Сделайте 3-4 фолдинга с охлаждением.\n4. Вырежьте треугольники и сверните.\n5. Выпекайте 20 мин при 200°C.",
                cookTimeMinutes = 180,
                servings = 12,
                difficulty = "Сложно",
                category = "baking",
                imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=800",
            ),
            RecipeEntity(
                authorId = ivan,
                title = "Ризотто с грибами",
                description = "Кремовое ризотто с лесными грибами и пармезаном.",
                ingredients = "Рис арборio — 300 г\nГрибы — 300 г\nЛук — 1 шт.\nБульон — 1 л\nБелое вино — 100 мл\nПармезан — 80 г\nСливочное масло — 40 г",
                steps = "1. Обжарьте лук и грибы.\n2. Добавьте рис, обжарьте 2 мин.\n3. Влейте вино, выпарите.\n4. Постепенно добавляйте бульон, помешивая.\n5. Завершите маслом и пармезаном.",
                cookTimeMinutes = 40,
                servings = 4,
                difficulty = "Средне",
                category = "italian",
                imageUrl = "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=800",
            ),
        )

        val recipeIds = recipes.map { recipeDao.insert(it) }

        likeDao.insert(LikeEntity(recipeId = recipeIds[0], chefId = me))
        likeDao.insert(LikeEntity(recipeId = recipeIds[0], chefId = ivan))
        likeDao.insert(LikeEntity(recipeId = recipeIds[0], chefId = maria))
        likeDao.insert(LikeEntity(recipeId = recipeIds[1], chefId = me))
        likeDao.insert(LikeEntity(recipeId = recipeIds[1], chefId = anna))
        likeDao.insert(LikeEntity(recipeId = recipeIds[2], chefId = me))
        likeDao.insert(LikeEntity(recipeId = recipeIds[3], chefId = dmitry))

        val commentDao = db.commentDao()
        commentDao.insert(
            CommentEntity(
                recipeId = recipeIds[0],
                authorId = ivan,
                text = "Обожаю медовик! Делаю по вашему рецепту каждый Новый год.",
            ),
        )
        commentDao.insert(
            CommentEntity(
                recipeId = recipeIds[0],
                authorId = maria,
                text = "Крем получился идеально густым. Спасибо за совет с охлаждением!",
            ),
        )
        commentDao.insert(
            CommentEntity(
                recipeId = recipeIds[1],
                authorId = anna,
                text = "Настоящая карбонара — без сливок! Браво 👏",
            ),
        )
        commentDao.insert(
            CommentEntity(
                recipeId = recipeIds[2],
                authorId = me,
                text = "Отличный обед на каждый день, быстро и полезно.",
            ),
        )
    }
}
