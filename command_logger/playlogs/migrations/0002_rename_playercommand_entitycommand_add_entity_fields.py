from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("playlogs", "0001_initial"),
    ]

    operations = [
        migrations.RenameModel(
            old_name="PlayerCommand",
            new_name="EntityCommand",
        ),
        migrations.AddField(
            model_name="entitycommand",
            name="entity_id",
            field=models.CharField(default="unknown", max_length=150),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name="entitycommand",
            name="entity_state",
            field=models.JSONField(blank=True, default=dict),
        ),
    ]
