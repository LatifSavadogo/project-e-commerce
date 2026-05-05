<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        if (Schema::hasTable('WebhookEvent')) {
            return;
        }

        Schema::create('WebhookEvent', function (Blueprint $table) {
            $table->id('id');
            $table->string('provider', 64);
            $table->string('provider_event_id', 191)->unique();
            $table->longText('payload');
            $table->timestamp('created_at')->nullable();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('WebhookEvent');
    }
};
